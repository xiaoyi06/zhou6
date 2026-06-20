package com.zhou6.cloud.sys.dto;

import java.util.List;

/**
 * 白名单批量删除参数。
 */
public class WhitelistDeleteDTO {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
