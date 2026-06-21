package com.zhou6.cloud.auth.client.fallback;

import com.zhou6.cloud.auth.client.SysAuditClient;
import com.zhou6.cloud.auth.dto.LoginLogRequest;
import com.zhou6.cloud.common.dto.R;
import org.springframework.stereotype.Component;

/**
 * 审计服务不可用时丢弃日志，避免审计链路影响认证结果。
 */
@Component
public class SysAuditClientFallback implements SysAuditClient {

    @Override
    public R<Void> publishLoginLog(LoginLogRequest request) {
        return R.ok(null);
    }
}
