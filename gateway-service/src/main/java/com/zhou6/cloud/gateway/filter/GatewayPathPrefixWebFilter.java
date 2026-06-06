package com.zhou6.cloud.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 网关统一路径前缀过滤器。
 * <p>
 * 外部接口统一要求携带固定前缀，例如 /hakunaMatata/api/v1/auth-api/login；网关在转发到下游微服务前
 * 自动剥离该前缀，因此各微服务 Controller 不需要感知网关前缀。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class GatewayPathPrefixWebFilter implements WebFilter {

    private final String pathPrefix;

    /**
     * 创建网关统一路径前缀过滤器。
     *
     * @param pathPrefix 外部访问统一前缀
     */
    public GatewayPathPrefixWebFilter(@Value("${zhou6.gateway.path-prefix:/hakunaMatata}") String pathPrefix) {
        this.pathPrefix = normalizePrefix(pathPrefix);
    }

    /**
     * 校验并剥离外部统一前缀。
     *
     * @param exchange 当前请求交换对象
     * @param chain WebFlux 过滤器链
     * @return 异步处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String rawPath = exchange.getRequest().getURI().getRawPath();
        if (!rawPath.equals(pathPrefix) && !rawPath.startsWith(pathPrefix + "/")) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        String strippedPath = rawPath.substring(pathPrefix.length());
        if (strippedPath.isBlank()) {
            strippedPath = "/";
        }
        if ("/swagger-ui.html".equals(strippedPath)
                || "/swagger-ui/index.html".equals(strippedPath)
                || "/swagger-ui/".equals(strippedPath)) {
            return redirectToSwaggerWebjar(exchange);
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .path(strippedPath)
                .headers(headers -> headers.set("X-Gateway-Path-Prefix", pathPrefix))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> redirectToSwaggerWebjar(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        exchange.getResponse().getHeaders().setLocation(UriComponentsBuilder.fromPath(
                        pathPrefix + "/webjars/swagger-ui/index.html")
                .queryParam("configUrl", pathPrefix + "/v3/api-docs/swagger-config")
                .build()
                .toUri());
        return exchange.getResponse().setComplete();
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank() || "/".equals(prefix.trim())) {
            return "";
        }
        String normalized = prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
