package com.zhou6.cloud.sys.dto;

/**
 * 白名单保存参数。
 */
public class WhitelistDTO {

    private String id;
    private String type;
    private String value;
    private Integer isStatus;
    private String remark;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
