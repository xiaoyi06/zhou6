package com.zhou6.cloud.sys.service;

import com.zhou6.cloud.sys.dto.LoginLogDTO;
import com.zhou6.cloud.sys.dto.LoginLogBatchDeleteDTO;
import com.zhou6.cloud.sys.dto.LoginLogDeleteDTO;
import com.zhou6.cloud.sys.dto.LoginLogQueryDTO;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.LoginLogVO;

public interface SysAuditService {

    void publishLoginLog(LoginLogDTO dto);

    PageResponse<LoginLogVO> loginLogPage(LoginLogQueryDTO dto);

    void deleteLoginLog(LoginLogDeleteDTO dto);

    void deleteLoginLogs(LoginLogBatchDeleteDTO dto);

    void flushLoginLogs();
}
