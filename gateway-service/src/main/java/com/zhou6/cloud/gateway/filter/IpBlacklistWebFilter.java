package com.zhou6.cloud.gateway.filter;

import java.net.InetSocketAddress;
import java.net.InetAddress;

import com.zhou6.cloud.gateway.support.GatewayErrorResponseWriter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 网关 IP 黑名单过滤器，在认证和路由前拦截已启用的封禁 IP。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IpBlacklistWebFilter implements WebFilter {

    private static final String IP_BLACKLIST_PREFIX = "zhou6:sys:ip-blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final GatewayErrorResponseWriter errorResponseWriter;

    public IpBlacklistWebFilter(ReactiveStringRedisTemplate redisTemplate,
            GatewayErrorResponseWriter errorResponseWriter) {
        this.redisTemplate = redisTemplate;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientIp = resolveClientIp(exchange);
        if (clientIp == null) {
            return chain.filter(exchange);
        }
        return redisTemplate.hasKey(IP_BLACKLIST_PREFIX + clientIp)
                // Redis 故障时按现有系统缓存策略放行，避免 Redis 故障造成全站不可用。
                .onErrorReturn(false)
                .flatMap(blocked -> Boolean.TRUE.equals(blocked)
                        ? errorResponseWriter.write(exchange, HttpStatus.FORBIDDEN, "当前 IP 已被禁止访问")
                        : chain.filter(exchange));
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return normalizeIp(forwardedFor.split(",")[0].trim());
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return null;
        }
        return normalizeIp(remoteAddress.getAddress().getHostAddress());
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || !ipAddress.matches("[0-9a-fA-F:.]+")) {
            return null;
        }
        try {
            return InetAddress.getByName(ipAddress).getHostAddress();
        } catch (Exception ex) {
            return null;
        }
    }
}
