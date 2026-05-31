package com.zhou6.cloud.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * OpenAPI 文档代理过滤器。
 * <p>
 * Swagger 聚合只需要读取各微服务的 /v3/api-docs，本过滤器在网关本地完成代理，
 * 避免开发环境下 Nacos 路由覆盖或服务发现选到旧实例导致文档加载失败。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenApiProxyWebFilter implements WebFilter {

    private final WebClient webClient;
    private final Map<String, String> openApiTargets;

    /**
     * 创建 OpenAPI 文档代理过滤器。
     *
     * @param builder WebClient 构造器
     * @param authOpenApiUrl auth-service 文档地址
     * @param userOpenApiUrl user-service 文档地址
     * @param fileOpenApiUrl file-service 文档地址
     */
    public OpenApiProxyWebFilter(WebClient.Builder builder,
            @Value("${zhou6.gateway.path-prefix:/hakunaMatata}") String pathPrefix,
            @Value("${zhou6.gateway.openapi.auth-url:http://localhost:53072/v3/api-docs}") String authOpenApiUrl,
            @Value("${zhou6.gateway.openapi.user-url:http://localhost:52048/user/v3/api-docs}") String userOpenApiUrl,
            @Value("${zhou6.gateway.openapi.file-url:http://localhost:52049/file/v3/api-docs}") String fileOpenApiUrl) {
        this.webClient = builder.build();
        String normalizedPrefix = normalizePrefix(pathPrefix);
        this.openApiTargets = Map.of(
                "/auth/v3/api-docs", authOpenApiUrl,
                "/user/v3/api-docs", userOpenApiUrl,
                "/file/v3/api-docs", fileOpenApiUrl,
                normalizedPrefix + "/auth/v3/api-docs", authOpenApiUrl,
                normalizedPrefix + "/user/v3/api-docs", userOpenApiUrl,
                normalizedPrefix + "/file/v3/api-docs", fileOpenApiUrl
        );
    }

    /**
     * 代理 Swagger 聚合页面请求的微服务 OpenAPI JSON。
     *
     * @param exchange 当前请求交换对象
     * @param chain WebFlux 过滤器链
     * @return 异步处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String targetUrl = openApiTargets.get(exchange.getRequest().getURI().getPath());
        if (targetUrl == null) {
            return chain.filter(exchange);
        }
        return webClient.get()
                .uri(targetUrl)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> writeJson(exchange.getResponse(), body))
                .onErrorResume(ex -> writeError(exchange.getResponse()));
    }

    private Mono<Void> writeJson(ServerHttpResponse response, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private Mono<Void> writeError(ServerHttpResponse response) {
        byte[] bytes = "{\"code\":\"050000\",\"message\":\"OpenAPI 文档读取失败\",\"data\":null,\"traceId\":null}"
                .getBytes(StandardCharsets.UTF_8);
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
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
