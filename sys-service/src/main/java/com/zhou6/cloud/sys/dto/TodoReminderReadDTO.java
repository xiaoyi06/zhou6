package com.zhou6.cloud.sys.dto;

import java.util.List;

/** 标记一个或多个提醒为已读。 */
public class TodoReminderReadDTO {

    private List<String> ids;

    public List<String> getIds() { return ids; }
    public void setIds(List<String> ids) { this.ids = ids; }
}
