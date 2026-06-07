package com.zhou6.cloud.sys.entity;

import java.time.LocalDateTime;

/**
 * API 调用频率统计实体，对应 sys_api_usage_stat 表。
 */
public class SysApiUsageStat {

    private Long userId;
    private String moduleName;
    private String displayName;
    private String apiPath;
    private String routePath;
    private String icon;
    private Long useCount;
    private LocalDateTime lastAccessTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }
    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Long getUseCount() { return useCount; }
    public void setUseCount(Long useCount) { this.useCount = useCount; }
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }
}
