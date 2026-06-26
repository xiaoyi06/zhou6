package com.zhou6.cloud.message.dto;

import java.util.List;

/**
 * 批量标记消息已读参数。
 */
public class MessageReadDTO {

    private List<String> ids;

    public List<String> getIds() { return ids; }
    public void setIds(List<String> ids) { this.ids = ids; }
}
