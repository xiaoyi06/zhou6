package com.zhou6.cloud.sys.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/** 管理员菜单访问汇总响应。 */
public class MenuUsageSummaryVO {

    private String moduleName;
    private String displayName;
    private String apiPath;
    private String routePath;
    private String icon;
    private Long totalUseCount;
    private Long userCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

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
    public Long getTotalUseCount() { return totalUseCount; }
    public void setTotalUseCount(Long totalUseCount) { this.totalUseCount = totalUseCount; }
    public Long getUserCount() { return userCount; }
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }
}
