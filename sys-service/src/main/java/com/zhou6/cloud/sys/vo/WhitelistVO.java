package com.zhou6.cloud.sys.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 白名单查询响应，ID 使用字符串避免前端大整数精度丢失。
 */
public class WhitelistVO {

    private String id;
    private String type;
    private String value;
    private Short isStatus;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Short getIsStatus() { return isStatus; }
    public void setIsStatus(Short isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
