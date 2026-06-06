package com.zhou6.cloud.sys.service;

import com.zhou6.cloud.sys.dto.LoginLogDTO;
import com.zhou6.cloud.sys.dto.PageQueryDTO;
import com.zhou6.cloud.sys.entity.SysLoginLog;
import com.zhou6.cloud.sys.vo.PageResponse;

public interface SysAuditService {

    void publishLoginLog(LoginLogDTO dto);

    PageResponse<SysLoginLog> loginLogPage(PageQueryDTO dto);

    void flushLoginLogs();
}
