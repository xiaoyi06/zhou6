package com.zhou6.cloud.sys.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.zhou6.cloud.sys.constant.SysRedisKeys;
import com.zhou6.cloud.sys.dto.MenuUsageDTO;
import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.entity.SysMenuUsageStat;
import com.zhou6.cloud.sys.mapper.SysMenuUsageStatMapper;
import com.zhou6.cloud.sys.service.SysMenuUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysMenuUsageServiceImpl extends BaseSysService implements SysMenuUsageService {

    private static final Logger log = LoggerFactory.getLogger(SysMenuUsageServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final SysMenuUsageStatMapper mapper;

    public SysMenuUsageServiceImpl(StringRedisTemplate redisTemplate, JdbcTemplate jdbcTemplate,
            SysMenuUsageStatMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public void record(MenuUsageDTO dto) {
        require(dto != null, "菜单使用参数不能为空");
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        Long menuId = parseRequiredId(dto.getMenuId(), "菜单ID不能为空");
        try {
            String key = SysRedisKeys.MENU_USAGE_PREFIX + userId + ":" + menuId;
            redisTemplate.opsForHash().increment(key, "useCount", 1L);
            redisTemplate.opsForHash().put(key, "userId", String.valueOf(userId));
            redisTemplate.opsForHash().put(key, "menuId", String.valueOf(menuId));
            redisTemplate.opsForHash().put(key, "menuName", dto.getMenuName() == null ? "" : dto.getMenuName());
            redisTemplate.opsForHash().put(key, "routePath", dto.getRoutePath() == null ? "" : dto.getRoutePath());
            redisTemplate.opsForHash().put(key, "lastUseTime", LocalDateTime.now().toString());
        } catch (Exception ex) {
            log.warn("Record menu usage failed: userId={}, menuId={}", userId, menuId, ex);
        }
    }
}
