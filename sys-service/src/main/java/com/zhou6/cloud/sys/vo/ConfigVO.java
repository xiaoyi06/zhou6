package com.zhou6.cloud.sys.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 系统配置查询响应，ID 使用字符串避免前端大整数精度丢失。
 */
public class ConfigVO {

    private String id;
    private String configKey;
    private String configValue;
    private String configName;
    private Short isStatus;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public Short getIsStatus() { return isStatus; }
    public void setIsStatus(Short isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
