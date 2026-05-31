package com.zhou6.cloud.gateway.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.ErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一错误响应写入器。
 *
 * <p>WebFlux 过滤器链中无法复用 MVC 的全局异常处理时，通过该组件输出统一的 R 响应体。</p>
 */
@Component
public class GatewayErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public GatewayErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 使用 HTTP 状态码作为业务错误码写出错误响应。
     *
     * @param exchange 当前请求交换对象
     * @param status HTTP 状态
     * @param message 错误提示
     * @return 响应写入结果
     */
    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        return write(exchange, status, String.valueOf(status.value()), message);
    }

    /**
     * 使用系统错误码写出错误响应。
     *
     * @param exchange 当前请求交换对象
     * @param errorCode 系统错误码
     * @return 响应写入结果
     */
    public Mono<Void> write(ServerWebExchange exchange, ErrorCode errorCode) {
        return write(exchange, HttpStatus.valueOf(errorCode.getHttpStatus()), errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 写出完整错误响应。
     *
     * @param exchange 当前请求交换对象
     * @param status HTTP 状态
     * @param code 业务错误码
     * @param message 错误提示
     * @return 响应写入结果
     */
    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] body = objectMapper.writeValueAsBytes(R.fail(code, message));
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            return response.setComplete();
        }
    }
}
