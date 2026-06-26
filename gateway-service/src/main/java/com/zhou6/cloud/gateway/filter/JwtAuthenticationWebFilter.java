package com.zhou6.cloud.gateway.filter;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.common.constant.ApiPathConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.common.handler.TokenException;
import com.zhou6.cloud.common.security.JwtClaims;
import com.zhou6.cloud.common.security.JwtTokenSupport;
import com.zhou6.cloud.common.security.LoginSession;
import com.zhou6.cloud.gateway.support.GatewayErrorResponseWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 网关 JWT 认证过滤器。
 *
 * <p>负责跳过白名单接口、解析访问令牌、校验 Redis 当前会话，并将用户 ID 写入响应式安全上下文。</p>
 */
@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String AUTH_API = ApiPathConstants.API_V1 + "/auth-api";

    private static final List<String> PERMIT_PATHS = List.of(
            AUTH_API + "/login",
            AUTH_API + "/refresh",
            AUTH_API + "/logout",
            "/auth/v3/api-docs",
            "/user/v3/api-docs",
            "/file/v3/api-docs",
            "/account/v3/api-docs",
            "/order/v3/api-docs",
            "/workflow/v3/api-docs",
            "/message/v3/api-docs",
            "/favicon.ico"
    );

    private static final List<String> PERMIT_PATH_PREFIXES = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    private final JwtTokenSupport jwtSupport;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final GatewayErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationWebFilter(@Value("${jwt.secret}") String jwtSecret,
            ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            GatewayErrorResponseWriter errorResponseWriter) {
        this.jwtSupport = new JwtTokenSupport(jwtSecret);
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPermitPath(path)) {
            // 白名单接口不校验 JWT，但仍透传客户端 IP，供 Auth 服务做单 IP 登录限制。
            return chain.filter(withClientIp(exchange));
        }

        try {
            // 非白名单接口必须携带短效 JWT，并且 JWT 中的 sessionId 必须仍是 Redis 中的当前会话。
            JwtClaims claims = jwtSupport.verifyAndGetClaims(resolveToken(exchange));
            ServerWebExchange authenticatedExchange = withAuthenticatedHeaders(exchange, claims.getUserId());
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(claims.getUserId(), null, List.of());
            // 将认证结果写入响应式安全上下文，交给 SecurityWebFilterChain 完成 authenticated 判断。
            return redisTemplate.opsForValue().get(currentSessionKey(claims.getUserId()))
                    .map(this::readLoginSession)
                    .filter(session -> Objects.equals(claims.getSessionId(), session.getSessionId()))
                    .switchIfEmpty(Mono.error(new BizException(CommonErrorCode.LOGIN_SESSION_EXPIRED)))
                    .then(chain.filter(authenticatedExchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                    Mono.just(new SecurityContextImpl(authentication)))))
                    .onErrorResume(BizException.class, ex -> writeBizError(exchange, ex))
                    .onErrorResume(ResponseStatusException.class, ex -> writeError(exchange, ex))
                    .onErrorResume(TokenException.class, ex -> writeUnauthorized(exchange));
        } catch (BizException ex) {
            return writeBizError(exchange, ex);
        } catch (ResponseStatusException ex) {
            return writeError(exchange, ex);
        } catch (TokenException ex) {
            return writeUnauthorized(exchange);
        }
    }

    private Mono<Void> writeError(ServerWebExchange exchange, ResponseStatusException ex) {
        String message = ex.getReason() == null ? "请求处理失败" : ex.getReason();
        return errorResponseWriter.write(exchange, HttpStatus.valueOf(ex.getStatusCode().value()), message);
    }

    private Mono<Void> writeBizError(ServerWebExchange exchange, BizException ex) {
        return errorResponseWriter.write(exchange, HttpStatus.valueOf(ex.getHttpStatus()), ex.getCode(), ex.getMessage());
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        return errorResponseWriter.write(exchange, CommonErrorCode.TOKEN_INVALID);
    }

    private boolean isPermitPath(String path) {
        if (PERMIT_PATHS.contains(path)) {
            return true;
        }
        return PERMIT_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private ServerWebExchange withClientIp(ServerWebExchange exchange) {
        String clientIp = resolveClientIp(exchange);
        // 下游服务统一从 X-Client-Ip 读取网关识别出的客户端 IP。
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set("X-Client-Ip", clientIp))
                .build();
        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withAuthenticatedHeaders(ServerWebExchange exchange, String userId) {
        String clientIp = resolveClientIp(exchange);
        // 覆盖客户端伪造的用户头，只把已校验 JWT 中的用户 ID 传给下游服务。
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.set("X-Client-Ip", clientIp);
                    headers.set("X-User-Id", userId);
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private String resolveToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            String path = exchange.getRequest().getURI().getPath();
            String accessToken = exchange.getRequest().getQueryParams().getFirst("access_token");
            if (path.startsWith("/ws/") && accessToken != null && !accessToken.isBlank()) {
                return accessToken;
            }
            throw new BizException(CommonErrorCode.TOKEN_INVALID);
        }
        if (authorization.startsWith("Bearer_")) {
            return authorization.substring("Bearer_".length());
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        throw new BizException(CommonErrorCode.TOKEN_INVALID);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // 如果前面还有负载均衡或反向代理，优先使用最左侧原始客户端 IP。
            return forwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return "unknown";
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    private String currentSessionKey(String userId) {
        return "zhou6:auth:session:" + userId;
    }

    private LoginSession readLoginSession(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException(CommonErrorCode.LOGIN_SESSION_EXPIRED);
        }
        try {
            return objectMapper.readValue(value, LoginSession.class);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.LOGIN_SESSION_EXPIRED);
        }
    }
}
