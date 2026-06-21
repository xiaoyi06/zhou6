package com.zhou6.cloud.auth.client;

import com.zhou6.cloud.auth.client.fallback.SysAuditClientFallback;
import com.zhou6.cloud.auth.dto.LoginLogRequest;
import com.zhou6.cloud.common.constant.ApiPathConstants;
import com.zhou6.cloud.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统审计服务 Feign 客户端，供认证服务异步投递登录日志。
 */
@FeignClient(name = "sys-service", fallback = SysAuditClientFallback.class)
public interface SysAuditClient {

    String SYS_SERVICE_CONTEXT = "/sys";
    String AUDIT_API = SYS_SERVICE_CONTEXT + ApiPathConstants.API_V1 + "/sys-api/audit";

    @PostMapping(AUDIT_API + "/loginLog")
    R<Void> publishLoginLog(@RequestBody LoginLogRequest request);
}
