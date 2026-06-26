package com.zhou6.cloud.message.constant;

import com.zhou6.cloud.common.constant.ApiPathConstants;

/**
 * 消息中心接口路径常量。
 */
public final class MessageApiPathConstants {

    public static final String MESSAGE_API = ApiPathConstants.API_V1 + "/message-api";

    public static final String MESSAGE = MESSAGE_API + "/message";

    public static final String MESSAGE_INNER = MESSAGE_API + "/internal/message";

    private MessageApiPathConstants() {
    }
}
