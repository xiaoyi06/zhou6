package com.zhou6.cloud.common.handler;

import com.zhou6.cloud.common.dto.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 服务公共全局异常处理器。
 *
 * <p>所有引入 common-server 的 Servlet Web 服务会自动加载该处理器，
 * 将业务异常、令牌异常和未知异常统一转换成 R 响应体。</p>
 */
@RestControllerAdvice
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param ex 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException ex) {
        if (ex.getCause() == null) {
            log.warn("Handled business exception: code={}, httpStatus={}, message={}",
                    ex.getCode(), ex.getHttpStatus(), ex.getMessage(), ex);
        } else {
            log.error("Handled business exception with cause: code={}, httpStatus={}, message={}",
                    ex.getCode(), ex.getHttpStatus(), ex.getMessage(), ex);
        }
        return ResponseEntity.status(ex.getHttpStatus()).body(R.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理 Spring Web 状态异常。
     *
     * @param ex Web 状态异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<R<Void>> handleResponseStatusException(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        String message = ex.getReason() == null ? "请求处理失败" : ex.getReason();
        log.warn("Handled response status exception: status={}, message={}", code, message, ex);
        return ResponseEntity.status(ex.getStatusCode()).body(R.fail(code, message));
    }

    /**
     * 处理令牌异常。
     *
     * @param ex 令牌异常
     * @return 统一失败响应
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<R<Void>> handleTokenException(TokenException ex) {
        log.warn("Handled token exception: message={}", ex.getMessage(), ex);
        return error(CommonErrorCode.TOKEN_INVALID);
    }

    /**
     * 处理非法参数异常。
     *
     * @param ex 非法参数异常
     * @return 统一失败响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Handled illegal argument exception: message={}", ex.getMessage(), ex);
        return error(CommonErrorCode.PARAM_INVALID);
    }

    /**
     * 处理兜底系统异常。
     *
     * @param ex 未知异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception ex) {
        log.error("Unhandled servlet exception", ex);
        return error(CommonErrorCode.SYSTEM_ERROR);
    }

    private ResponseEntity<R<Void>> error(CommonErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(R.fail(errorCode.getCode(), errorCode.getMessage()));
    }
}
