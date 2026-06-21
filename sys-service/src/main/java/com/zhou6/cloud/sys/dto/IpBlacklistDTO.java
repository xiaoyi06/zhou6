package com.zhou6.cloud.sys.dto;

/**
 * IP 黑名单保存参数。
 */
public class IpBlacklistDTO {

    private String id;
    private String ipAddress;
    private Integer isStatus;
    private String remark;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
