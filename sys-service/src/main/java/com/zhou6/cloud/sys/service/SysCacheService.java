package com.zhou6.cloud.sys.service;

/**
 * 系统配置缓存服务。
 */
public interface SysCacheService {

    void refreshConfig(String configKey);

    void removeConfig(String configKey);

    String getConfigValue(String configKey);

    void refreshDict(String dictType);

    void refreshAllDicts();

    void refreshWhitelist(String type, String value, Short isStatus);

    void removeWhitelist(String type, String value);

    boolean isMaintenanceMode();

    boolean isWhitelisted(String type, String value);

    void refreshIpBlacklist(String ipAddress, Short isStatus);

    void removeIpBlacklist(String ipAddress);
}
