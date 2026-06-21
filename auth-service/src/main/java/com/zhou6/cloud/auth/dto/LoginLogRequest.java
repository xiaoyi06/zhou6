package com.zhou6.cloud.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 认证服务投递给系统审计服务的登录日志参数。
 */
@Data
@AllArgsConstructor
public class LoginLogRequest {

    private String userId;
    private String username;
    private String ipAddress;
    private String loginLocation;
    private String browser;
    private String os;
    private Integer status;
    private String msg;
}
