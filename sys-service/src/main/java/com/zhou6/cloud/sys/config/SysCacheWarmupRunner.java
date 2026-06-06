package com.zhou6.cloud.sys.config;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.sys.entity.SysConfig;
import com.zhou6.cloud.sys.entity.SysWhitelist;
import com.zhou6.cloud.sys.mapper.SysConfigMapper;
import com.zhou6.cloud.sys.mapper.SysWhitelistMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 系统服务启动后预热配置和白名单缓存，避免首次请求击穿数据库。
 */
@Component
public class SysCacheWarmupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SysCacheWarmupRunner.class);

    private final SysConfigMapper configMapper;
    private final SysWhitelistMapper whitelistMapper;
    private final SysCacheService cacheService;

    public SysCacheWarmupRunner(SysConfigMapper configMapper, SysWhitelistMapper whitelistMapper,
            SysCacheService cacheService) {
        this.configMapper = configMapper;
        this.whitelistMapper = whitelistMapper;
        this.cacheService = cacheService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<SysConfig> configs = configMapper.selectPage(null, null, Short.valueOf((short) 1), 10000, 0);
            for (SysConfig config : configs) {
                cacheService.refreshConfig(config.getConfigKey());
            }
            List<SysWhitelist> whitelists = whitelistMapper.selectList(new LambdaQueryWrapper<SysWhitelist>()
                    .eq(SysWhitelist::getIsStatus, Short.valueOf((short) 1)));
            for (SysWhitelist whitelist : whitelists) {
                cacheService.refreshWhitelist(whitelist.getType(), whitelist.getValue(), whitelist.getIsStatus());
            }
            log.info("Sys cache warmup finished: configCount={}, whitelistCount={}", configs.size(), whitelists.size());
        } catch (Exception ex) {
            log.warn("Sys cache warmup failed, service will use degrade strategy before cache is ready", ex);
        }
    }
}
