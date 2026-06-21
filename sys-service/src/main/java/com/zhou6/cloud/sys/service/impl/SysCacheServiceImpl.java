package com.zhou6.cloud.sys.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.sys.constant.SysRedisKeys;
import com.zhou6.cloud.sys.entity.SysConfig;
import com.zhou6.cloud.sys.entity.SysDictData;
import com.zhou6.cloud.sys.entity.SysDictType;
import com.zhou6.cloud.sys.mapper.SysConfigMapper;
import com.zhou6.cloud.sys.mapper.SysDictDataMapper;
import com.zhou6.cloud.sys.mapper.SysDictTypeMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import com.zhou6.cloud.sys.vo.DictDataVO;
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
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final ObjectMapper objectMapper;

    public SysCacheServiceImpl(StringRedisTemplate redisTemplate, SysConfigMapper configMapper,
            SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.configMapper = configMapper;
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
        this.objectMapper = objectMapper;
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
    public String getConfigValue(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(SysRedisKeys.CONFIG_PREFIX + configKey);
        } catch (Exception ex) {
            log.warn("Read sys config cache failed: key={}", configKey, ex);
            return null;
        }
    }

    @Override
    public void refreshDict(String dictType) {
        if (dictType == null || dictType.isBlank()) {
            return;
        }
        String redisKey = SysRedisKeys.DICT_PREFIX + dictType;
        try {
            if (!isDictRedisSyncEnabled()) {
                redisTemplate.delete(redisKey);
                return;
            }
            List<DictDataVO> dictData = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, dictType)
                    .eq(SysDictData::getIsStatus, Short.valueOf((short) 1))
                    .orderByAsc(SysDictData::getDictSort)
                    .orderByAsc(SysDictData::getId)).stream().map(this::toDictDataVo).toList();
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(dictData), CACHE_TTL);
        } catch (Exception ex) {
            log.warn("Refresh sys dict cache failed: dictType={}", dictType, ex);
        }
    }

    @Override
    public void refreshAllDicts() {
        for (SysDictType dictType : dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>())) {
            refreshDict(dictType.getDictType());
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

    @Override
    public void refreshIpBlacklist(String ipAddress, Short isStatus) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        try {
            String key = SysRedisKeys.IP_BLACKLIST_PREFIX + ipAddress;
            if (Objects.equals(isStatus, Short.valueOf((short) 1))) {
                redisTemplate.opsForValue().set(key, "1", CACHE_TTL);
            } else {
                redisTemplate.delete(key);
            }
        } catch (Exception ex) {
            log.warn("Refresh IP blacklist cache failed: ipAddress={}", ipAddress, ex);
        }
    }

    @Override
    public void removeIpBlacklist(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        try {
            redisTemplate.delete(SysRedisKeys.IP_BLACKLIST_PREFIX + ipAddress);
        } catch (Exception ex) {
            log.warn("Remove IP blacklist cache failed: ipAddress={}", ipAddress, ex);
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

    private boolean isDictRedisSyncEnabled() {
        return "true".equalsIgnoreCase(stripJsonString(getConfigValue("sys.dict.redis.sync")));
    }

    private DictDataVO toDictDataVo(SysDictData entity) {
        DictDataVO vo = new DictDataVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setDictSort(entity.getDictSort());
        vo.setIsDefault(entity.getIsDefault());
        vo.setIsStatus(entity.getIsStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
