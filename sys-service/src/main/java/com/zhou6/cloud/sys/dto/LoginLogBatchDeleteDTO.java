package com.zhou6.cloud.sys.dto;

import java.util.List;

/**
 * 批量删除登录日志参数。
 */
public class LoginLogBatchDeleteDTO {

    private List<LoginLogDeleteDTO> logs;

    public List<LoginLogDeleteDTO> getLogs() { return logs; }
    public void setLogs(List<LoginLogDeleteDTO> logs) { this.logs = logs; }
}
