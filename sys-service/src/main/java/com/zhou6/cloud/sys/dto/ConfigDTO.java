package com.zhou6.cloud.sys.dto;

/**
 * 系统配置保存参数。
 */
public class ConfigDTO {

    private String id;
    private String configKey;
    private String configValue;
    private String configName;
    private Integer isStatus;
    private String remark;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
