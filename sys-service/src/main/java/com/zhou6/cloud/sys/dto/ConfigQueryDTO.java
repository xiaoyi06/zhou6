package com.zhou6.cloud.sys.dto;

/**
 * 系统配置查询参数。
 */
public class ConfigQueryDTO extends PageQueryDTO {

    private String configKey;
    private String configName;
    private Integer isStatus;

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public Integer getIsStatus() { return isStatus; }
    public void setIsStatus(Integer isStatus) { this.isStatus = isStatus; }
}
