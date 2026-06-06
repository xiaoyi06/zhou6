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

    @Override
    public void flush() {
        try {
            Set<String> keys = redisTemplate.keys(SysRedisKeys.MENU_USAGE_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            for (String key : keys) {
                Long userId = parseLong(hashValue(key, "userId"));
                Long menuId = parseLong(hashValue(key, "menuId"));
                if (userId == null || menuId == null) {
                    continue;
                }
                long count = parseLong(hashValue(key, "useCount")) == null ? 0 : parseLong(hashValue(key, "useCount"));
                String lastUseTime = hashValue(key, "lastUseTime");
                jdbcTemplate.update("""
                        INSERT INTO sys_menu_usage_stat (user_id, menu_id, menu_name, route_path, use_count, last_use_time)
                        VALUES (?, ?, ?, ?, ?, ?)
                        ON CONFLICT (user_id, menu_id) DO UPDATE
                        SET menu_name = EXCLUDED.menu_name,
                            route_path = EXCLUDED.route_path,
                            use_count = sys_menu_usage_stat.use_count + EXCLUDED.use_count,
                            last_use_time = GREATEST(sys_menu_usage_stat.last_use_time, EXCLUDED.last_use_time)
                        """, userId, menuId, hashValue(key, "menuName"), hashValue(key, "routePath"), count,
                        Timestamp.valueOf(hasText(lastUseTime) ? LocalDateTime.parse(lastUseTime) : LocalDateTime.now()));
                redisTemplate.delete(key);
            }
        } catch (Exception ex) {
            log.warn("Flush menu usage stats failed", ex);
        }
    }

    @Override
    public List<SysMenuUsageStat> frequent(MenuUsageQueryDTO dto) {
        require(dto != null, "常用菜单查询参数不能为空");
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 10 : Math.min(dto.getLimit(), 50);
        return mapper.selectFrequent(userId, limit);
    }

    private String hashValue(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value == null ? null : String.valueOf(value);
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
