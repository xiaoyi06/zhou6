package com.zhou6.cloud.common.handler;

public interface ErrorCode {

    String getCode();

    int getHttpStatus();

    String getMessage();
}
