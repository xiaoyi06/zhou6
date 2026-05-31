package com.zhou6.cloud.gateway.support;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.common.handler.TokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GatewayGlobalExceptionHandler.class);

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
            if (bizException.getCause() == null) {
                log.warn("Handled gateway business exception: code={}, httpStatus={}, message={}",
                        bizException.getCode(), bizException.getHttpStatus(), bizException.getMessage(), bizException);
            } else {
                log.error("Handled gateway business exception with cause: code={}, httpStatus={}, message={}",
                        bizException.getCode(), bizException.getHttpStatus(), bizException.getMessage(), bizException);
            }
            return errorResponseWriter.write(exchange, HttpStatus.valueOf(bizException.getHttpStatus()),
                    bizException.getCode(), bizException.getMessage());
        }
        if (ex instanceof ResponseStatusException statusException) {
            String message = statusException.getReason() == null ? "请求处理失败" : statusException.getReason();
            return errorResponseWriter.write(exchange, HttpStatus.valueOf(statusException.getStatusCode().value()), message);
        }
        log.error("Unhandled gateway exception", ex);
        return errorResponseWriter.write(exchange, CommonErrorCode.SYSTEM_ERROR);
    }
}
