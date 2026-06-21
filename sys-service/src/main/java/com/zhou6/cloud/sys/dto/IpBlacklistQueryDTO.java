package com.zhou6.cloud.sys.dto;

/**
 * IP 黑名单查询参数。
 */
public class IpBlacklistQueryDTO extends PageQueryDTO {

    private String ipAddress;
    private Integer isStatus;

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
}
