package com.zhou6.cloud.sys.vo;

import java.time.LocalDateTime;

/**
 * IP 黑名单查询响应，ID 使用字符串避免前端大整数精度丢失。
 */
public class IpBlacklistVO {

    private String id;
    private String ipAddress;
    private Short isStatus;
    private String remark;
    private LocalDateTime createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Short getIsStatus() { return isStatus; }
    public void setIsStatus(Short isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
