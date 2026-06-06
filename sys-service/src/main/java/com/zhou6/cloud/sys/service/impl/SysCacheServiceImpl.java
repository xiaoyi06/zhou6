package com.zhou6.cloud.sys.service.impl;

import java.time.Duration;
import java.util.Objects;

import com.zhou6.cloud.sys.constant.SysRedisKeys;
import com.zhou6.cloud.sys.entity.SysConfig;
import com.zhou6.cloud.sys.mapper.SysConfigMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 系统配置缓存实现，Redis 异常时遵循默认放行降级策略。
 */
@Service
public class SysCacheServiceImpl implements SysCacheService {

    private static final Logger log = LoggerFactory.getLogger(SysCacheServiceImpl.class);
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    private final StringRedisTemplate redisTemplate;
    private final SysConfigMapper configMapper;

    public SysCacheServiceImpl(StringRedisTemplate redisTemplate, SysConfigMapper configMapper) {
        this.redisTemplate = redisTemplate;
        this.configMapper = configMapper;
    }

    @Override
    public void refreshConfig(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return;
        }
        SysConfig config = configMapper.selectByKey(configKey);
        try {
            if (config == null || !Objects.equals(config.getIsStatus(), Short.valueOf((short) 1))) {
                redisTemplate.delete(SysRedisKeys.CONFIG_PREFIX + configKey);
                return;
            }
            redisTemplate.opsForValue().set(SysRedisKeys.CONFIG_PREFIX + configKey, config.getConfigValue(), CACHE_TTL);
        } catch (Exception ex) {
            log.warn("Refresh sys config cache failed: key={}", configKey, ex);
        }
    }

    @Override
    public void removeConfig(String configKey) {
        try {
            redisTemplate.delete(SysRedisKeys.CONFIG_PREFIX + configKey);
        } catch (Exception ex) {
            log.warn("Remove sys config cache failed: key={}", configKey, ex);
        }
    }

    @Override
    public void refreshWhitelist(String type, String value, Short isStatus) {
        try {
            String key = SysRedisKeys.WHITELIST_PREFIX + type + ":" + value;
            if (Objects.equals(isStatus, Short.valueOf((short) 1))) {
                redisTemplate.opsForValue().set(key, "1", CACHE_TTL);
            } else {
                redisTemplate.delete(key);
            }
        } catch (Exception ex) {
            log.warn("Refresh whitelist cache failed: type={}, value={}", type, value, ex);
        }
    }

    @Override
    public void removeWhitelist(String type, String value) {
        try {
            redisTemplate.delete(SysRedisKeys.WHITELIST_PREFIX + type + ":" + value);
        } catch (Exception ex) {
            log.warn("Remove whitelist cache failed: type={}, value={}", type, value, ex);
        }
    }

    @Override
    public boolean isMaintenanceMode() {
        try {
            String value = redisTemplate.opsForValue().get(SysRedisKeys.CONFIG_PREFIX + "sys.maintenance.mode");
            return "true".equalsIgnoreCase(stripJsonString(value));
        } catch (Exception ex) {
            log.warn("Read maintenance mode failed, pass request by degrade strategy", ex);
            return false;
        }
    }

    @Override
    public boolean isWhitelisted(String type, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(SysRedisKeys.WHITELIST_PREFIX + type + ":" + value));
        } catch (Exception ex) {
            log.warn("Read whitelist failed, pass request by degrade strategy: type={}, value={}", type, value, ex);
            return true;
        }
    }

    private String stripJsonString(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
