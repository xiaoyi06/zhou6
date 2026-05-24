package com.zhou6.cloud.auth.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.common.handler.TokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(R.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<R<Void>> handleResponseStatusException(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        String message = ex.getReason() == null ? "请求处理失败" : ex.getReason();
        return ResponseEntity.status(ex.getStatusCode()).body(R.fail(code, message));
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<R<Void>> handleTokenException(TokenException ex) {
        return error(CommonErrorCode.TOKEN_INVALID);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return error(CommonErrorCode.PARAM_INVALID);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception ex) {
        return error(CommonErrorCode.SYSTEM_ERROR);
    }

    private ResponseEntity<R<Void>> error(CommonErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(R.fail(errorCode.getCode(), errorCode.getMessage()));
    }
}
