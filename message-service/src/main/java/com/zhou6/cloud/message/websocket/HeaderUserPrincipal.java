package com.zhou6.cloud.message.websocket;

import java.security.Principal;

/**
 * WebSocket 当前用户身份。
 */
public class HeaderUserPrincipal implements Principal {

    private final String name;

    public HeaderUserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
