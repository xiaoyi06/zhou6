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

@Component
public class GatewayErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public GatewayErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        return write(exchange, status, status.value(), message);
    }

    public Mono<Void> write(ServerWebExchange exchange, ErrorCode errorCode) {
        return write(exchange, HttpStatus.valueOf(errorCode.getHttpStatus()), errorCode.getCode(), errorCode.getMessage());
    }

    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, int code, String message) {
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
