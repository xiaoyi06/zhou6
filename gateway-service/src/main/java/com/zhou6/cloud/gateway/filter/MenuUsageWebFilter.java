package com.zhou6.cloud.gateway.filter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import com.zhou6.cloud.common.security.JwtTokenSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关菜单/应用层调用统计过滤器。
 *
 * <p>在请求完成后（仅 2xx），从请求路径提取模块和资源（如 user-api/role 对应角色管理），
 * 解析 JWT 获取用户 ID，将调用次数写入 Redis。sys-service 的定时任务每 5 分钟
 * 刷入 PostgreSQL {@code sys_api_usage_stat} 表。</p>
 */
@Component
public class MenuUsageWebFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MenuUsageWebFilter.class);

    private static final Pattern MODULE_PATTERN = Pattern.compile("/api/v1/([^/]+)");

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("/api/v1/[^/]+/([^/]+)");

    private static final List<String> SKIP_PATHS = List.of(
            "/api/v1/auth-api/login",
            "/api/v1/auth-api/refresh",
            "/api/v1/auth-api/logout",
            "/favicon.ico"
    );

    private static final List<String> SKIP_PREFIXES = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars",
            "/api/v1/sys-api/traffic",
            "/api/v1/sys-api/menuUsage",
            "/api/v1/sys-api/apiUsage"
    );

    private static final String REDIS_PREFIX = "zhou6:sys:api:usage:";

    private final JwtTokenSupport jwtSupport;
    private final ReactiveStringRedisTemplate redisTemplate;

    public MenuUsageWebFilter(@Value("${jwt.secret}") String jwtSecret,
            ReactiveStringRedisTemplate redisTemplate) {
        this.jwtSupport = new JwtTokenSupport(jwtSecret);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();

        String menuKey = resolveMenuKey(path);
        if (menuKey == null) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange).then(Mono.defer(() -> {
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            if (status == null || !status.is2xxSuccessful()) {
                return Mono.empty();
            }
            String userId = extractUserId(exchange);
            if (userId == null) {
                return Mono.empty();
            }
            return incrementUsage(userId, menuKey, path);
        })).onErrorResume(ex -> {
            log.warn("Record menu usage failed: path={}", path, ex);
            return Mono.empty();
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 从请求路径提取菜单标识，格式为 {应用}/{菜单}。
     * 例: /api/v1/user-api/role/page → user-api/role
     *     /api/v1/user-api/user/list  → user-api/user
     *     /api/v1/sys-api/dict/page   → sys-api/dict
     */
    private String resolveMenuKey(String path) {
        if (path == null) return null;
        if (SKIP_PATHS.contains(path)) return null;
        for (String prefix : SKIP_PREFIXES) {
            if (path.startsWith(prefix)) return null;
        }
        var moduleMatcher = MODULE_PATTERN.matcher(path);
        if (!moduleMatcher.find()) return null;
        String module = moduleMatcher.group(1);
        var resourceMatcher = RESOURCE_PATTERN.matcher(path);
        String resource = resourceMatcher.find() ? resourceMatcher.group(1) : module;
        return module + "/" + resource;
    }

    private String extractUserId(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank()) return null;
        try {
            String token;
            if (authHeader.startsWith("Bearer_")) {
                token = authHeader.substring("Bearer_".length());
            } else if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring("Bearer ".length());
            } else {
                return null;
            }
            return jwtSupport.verifyAndGetClaims(token).getUserId();
        } catch (Exception ex) {
            return null;
        }
    }

    private Mono<Void> incrementUsage(String userId, String menuKey, String path) {
        String key = REDIS_PREFIX + userId + ":" + menuKey;
        String now = LocalDateTime.now().toString();
        return redisTemplate.opsForHash().increment(key, "useCount", 1L)
                .flatMap(count -> {
                    Mono<Boolean> m1 = redisTemplate.opsForHash().put(key, "userId", userId);
                    Mono<Boolean> m2 = redisTemplate.opsForHash().put(key, "menuKey", menuKey);
                    Mono<Boolean> m3 = redisTemplate.opsForHash().put(key, "apiPath", path);
                    Mono<Boolean> m4 = redisTemplate.opsForHash().put(key, "lastAccessTime", now);
                    return Mono.when(m1, m2, m3, m4);
                })
                .onErrorResume(ex -> {
                    log.warn("Redis increment failed for key={}", key, ex);
                    return Mono.empty();
                })
                .then();
    }
}
