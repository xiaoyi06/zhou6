package com.zhou6.cloud.sys.task;

import com.zhou6.cloud.sys.service.SysAuditService;
import com.zhou6.cloud.sys.service.SysMenuUsageService;
import com.zhou6.cloud.sys.service.SysTrafficService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 系统服务批量刷库任务。
 */
@Component
public class SysFlushTask {

    private final SysAuditService auditService;
    private final SysTrafficService trafficService;
    private final SysMenuUsageService menuUsageService;

    public SysFlushTask(SysAuditService auditService, SysTrafficService trafficService,
            SysMenuUsageService menuUsageService) {
        this.auditService = auditService;
        this.trafficService = trafficService;
        this.menuUsageService = menuUsageService;
    }

    @Scheduled(fixedDelayString = "${zhou6.sys.login-log.flush-interval-ms:3000}")
    public void flushLoginLogs() {
        auditService.flushLoginLogs();
    }

    @Scheduled(cron = "${zhou6.sys.traffic.flush-cron:0 */5 * * * *}")
    public void flushTrafficStats() {
        trafficService.flush();
    }

    @Scheduled(cron = "${zhou6.sys.menu.flush-cron:0 */5 * * * *}")
    public void flushMenuUsageStats() {
        menuUsageService.flush();
    }
}
