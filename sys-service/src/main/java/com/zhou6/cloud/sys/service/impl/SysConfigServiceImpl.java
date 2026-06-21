package com.zhou6.cloud.sys.service.impl;

import java.util.Objects;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.sys.dto.ConfigDTO;
import com.zhou6.cloud.sys.dto.ConfigQueryDTO;
import com.zhou6.cloud.sys.entity.SysConfig;
import com.zhou6.cloud.sys.mapper.SysConfigMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import com.zhou6.cloud.sys.service.SysConfigService;
import com.zhou6.cloud.sys.vo.ConfigVO;
import com.zhou6.cloud.sys.vo.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl extends BaseSysService implements SysConfigService {

    private final SysConfigMapper configMapper;
    private final SysCacheService cacheService;
    private final IdentifierGenerator identifierGenerator;
    private final ObjectMapper objectMapper;

    public SysConfigServiceImpl(SysConfigMapper configMapper, SysCacheService cacheService,
            IdentifierGenerator identifierGenerator, ObjectMapper objectMapper) {
        this.configMapper = configMapper;
        this.cacheService = cacheService;
        this.identifierGenerator = identifierGenerator;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<ConfigVO> page(ConfigQueryDTO dto) {
        ConfigQueryDTO query = dto == null ? new ConfigQueryDTO() : dto;
        long pageNum = pageNum(query.getPageNum());
        long pageSize = pageSize(query.getPageSize());
        Short status = query.getIsStatus() == null ? null : query.getIsStatus().shortValue();
        long total = configMapper.countPage(blankToNull(query.getConfigKey()), blankToNull(query.getConfigName()), status);
        return new PageResponse<>(total, pageNum, pageSize,
                configMapper.selectPage(blankToNull(query.getConfigKey()), blankToNull(query.getConfigName()), status,
                        pageSize, (pageNum - 1) * pageSize).stream().map(this::toVo).toList());
    }

    @Override
    public void add(ConfigDTO dto) {
        requireValid(dto);
        require(configMapper.selectByKey(dto.getConfigKey()) == null, "配置键名已存在");
        SysConfig config = fill(new SysConfig(), dto);
        config.setId(identifierGenerator.nextId(config).longValue());
        configMapper.insert(config);
        refreshRelatedCaches(config.getConfigKey());
    }

    @Override
    public void edit(ConfigDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "配置ID不能为空");
        requireValid(dto);
        SysConfig old = configMapper.selectById(id);
        require(old != null, "配置不存在");
        SysConfig existed = configMapper.selectByKey(dto.getConfigKey());
        require(existed == null || Objects.equals(existed.getId(), id), "配置键名已存在");
        String oldConfigKey = old.getConfigKey();
        SysConfig config = fill(old, dto);
        configMapper.update(config);
        if (!Objects.equals(oldConfigKey, config.getConfigKey())) {
            cacheService.removeConfig(oldConfigKey);
        }
        refreshRelatedCaches(config.getConfigKey());
        if ("sys.dict.redis.sync".equals(oldConfigKey) && !Objects.equals(oldConfigKey, config.getConfigKey())) {
            cacheService.refreshAllDicts();
        }
    }

    @Override
    public void delete(String id) {
        Long configId = parseRequiredId(id, "配置ID不能为空");
        SysConfig old = configMapper.selectById(configId);
        require(old != null, "配置不存在");
        configMapper.deleteById(configId);
        cacheService.removeConfig(old.getConfigKey());
        if ("sys.dict.redis.sync".equals(old.getConfigKey())) {
            cacheService.refreshAllDicts();
        }
    }

    @Override
    public void refreshCache(String key) {
        require(hasText(key), "配置键名不能为空");
        refreshRelatedCaches(key);
    }

    private void requireValid(ConfigDTO dto) {
        require(dto != null, "配置参数不能为空");
        require(hasText(dto.getConfigKey()), "配置键名不能为空");
        require(hasText(dto.getConfigName()), "配置名称不能为空");
        require(hasText(dto.getConfigValue()), "配置值不能为空");
        try {
            objectMapper.readTree(dto.getConfigValue());
        } catch (Exception ex) {
            require(false, "配置值必须是合法JSON");
        }
    }

    private SysConfig fill(SysConfig config, ConfigDTO dto) {
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigName(dto.getConfigName());
        config.setIsStatus(toStatus(dto.getIsStatus()));
        config.setRemark(dto.getRemark());
        return config;
    }

    private ConfigVO toVo(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        vo.setId(String.valueOf(config.getId()));
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigName(config.getConfigName());
        vo.setIsStatus(config.getIsStatus());
        vo.setRemark(config.getRemark());
        vo.setUpdateTime(config.getUpdateTime());
        return vo;
    }

    private String blankToNull(String value) {
        return hasText(value) ? value : null;
    }

    private void refreshRelatedCaches(String configKey) {
        cacheService.refreshConfig(configKey);
        if ("sys.dict.redis.sync".equals(configKey)) {
            cacheService.refreshAllDicts();
        }
    }
}
