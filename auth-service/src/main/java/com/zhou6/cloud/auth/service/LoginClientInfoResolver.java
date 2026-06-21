package com.zhou6.cloud.auth.service;

import org.springframework.stereotype.Component;

/**
 * 从浏览器请求头中提取适合审计展示的终端信息。
 */
@Component
public class LoginClientInfoResolver {

    public LoginClientInfo resolve(String userAgent) {
        String value = userAgent == null ? "" : userAgent;
        return new LoginClientInfo(resolveBrowser(value), resolveOs(value));
    }

    private String resolveBrowser(String userAgent) {
        if (userAgent.contains("Edg/")) {
            return "Microsoft Edge";
        }
        if (userAgent.contains("OPR/") || userAgent.contains("Opera")) {
            return "Opera";
        }
        if (userAgent.contains("Chrome/") || userAgent.contains("CriOS/")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox/") || userAgent.contains("FxiOS/")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) {
            return "Safari";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            return "Internet Explorer";
        }
        return "未知浏览器";
    }

    private String resolveOs(String userAgent) {
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        if (userAgent.contains("Mac OS X")) {
            return "macOS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "未知系统";
    }

    public record LoginClientInfo(String browser, String os) {
    }
}
