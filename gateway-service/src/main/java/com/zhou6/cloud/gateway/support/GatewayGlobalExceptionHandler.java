package com.zhou6.cloud.gateway.support;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.common.handler.TokenException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Order(-2)
@Component
public class GatewayGlobalExceptionHandler implements WebExceptionHandler {

    private final GatewayErrorResponseWriter errorResponseWriter;

    public GatewayGlobalExceptionHandler(GatewayErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        if (ex instanceof TokenException) {
            return errorResponseWriter.write(exchange, CommonErrorCode.TOKEN_INVALID);
        }
        if (ex instanceof BizException bizException) {
            return errorResponseWriter.write(exchange, HttpStatus.valueOf(bizException.getHttpStatus()),
                    bizException.getCode(), bizException.getMessage());
        }
        if (ex instanceof ResponseStatusException statusException) {
            String message = statusException.getReason() == null ? "请求处理失败" : statusException.getReason();
            return errorResponseWriter.write(exchange, HttpStatus.valueOf(statusException.getStatusCode().value()), message);
        }
        return errorResponseWriter.write(exchange, CommonErrorCode.SYSTEM_ERROR);
    }
}
