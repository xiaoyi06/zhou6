package com.zhou6.cloud.sys.dto;

/**
 * 白名单查询参数。
 */
public class WhitelistQueryDTO extends PageQueryDTO {

    private String type;
    private String value;
    private Integer isStatus;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
}
