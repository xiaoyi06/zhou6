package com.zhou6.cloud.sys.vo;

import java.time.LocalDateTime;

/** 管理员查看指定菜单访问人员的响应。 */
public class MenuUsageUserVO {

    private String userId;
    private String username;
    private String nickname;
    private Long useCount;
    private LocalDateTime lastAccessTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Long getUseCount() { return useCount; }
    public void setUseCount(Long useCount) { this.useCount = useCount; }
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }
}
