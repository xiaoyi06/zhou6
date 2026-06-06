package com.zhou6.cloud.sys.entity;

import java.time.LocalDateTime;

/**
 * 系统控制配置实体，对应 sys_config 表。
 */
public class SysConfig {

    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private Short isStatus;
    private String remark;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
