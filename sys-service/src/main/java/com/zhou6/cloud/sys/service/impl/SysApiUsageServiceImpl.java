package com.zhou6.cloud.sys.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.sys.constant.SysRedisKeys;
import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.entity.SysApiUsageStat;
import com.zhou6.cloud.sys.entity.SysDictData;
import com.zhou6.cloud.sys.mapper.SysApiUsageStatMapper;
import com.zhou6.cloud.sys.mapper.SysDictDataMapper;
import com.zhou6.cloud.sys.mapper.SysMenuMapper;
import com.zhou6.cloud.sys.mapper.SysMenuMapper.MenuRouteIcon;
import com.zhou6.cloud.sys.service.SysApiUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysApiUsageServiceImpl extends BaseSysService implements SysApiUsageService {

    private static final Logger log = LoggerFactory.getLogger(SysApiUsageServiceImpl.class);
    private static final String DICT_TYPE = "sys_api_module_name";

    private final StringRedisTemplate redisTemplate;
    private final SysApiUsageStatMapper mapper;
    private final SysDictDataMapper dictDataMapper;
    private final SysMenuMapper sysMenuMapper;

    public SysApiUsageServiceImpl(StringRedisTemplate redisTemplate, SysApiUsageStatMapper mapper,
            SysDictDataMapper dictDataMapper, SysMenuMapper sysMenuMapper) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
        this.dictDataMapper = dictDataMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    @Override
    public List<SysApiUsageStat> frequent(MenuUsageQueryDTO dto) {
        require(dto != null, "常用菜单查询参数不能为空");
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 10 : Math.min(dto.getLimit(), 50);

        List<SysApiUsageStat> stats = mapper.selectFrequent(userId, limit);
        if (stats.isEmpty()) {
            return stats;
        }

        // 加载字典：moduleName → menuId(remark)
        Map<String, Long> menuIdMap = loadMenuIdMap();
        if (menuIdMap.isEmpty()) {
            return stats;
        }

        // 批量查 sys_menu 获取 routePath、icon、menuName
        List<Long> menuIds = stats.stream()
                .map(s -> menuIdMap.get(s.getModuleName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, MenuRouteIcon> menuMap = menuIds.stream()
                .map(sysMenuMapper::selectRouteIconById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MenuRouteIcon::getMenuId, r -> r));

        for (SysApiUsageStat stat : stats) {
            Long menuId = menuIdMap.get(stat.getModuleName());
            if (menuId == null) {
                continue;
            }
            MenuRouteIcon menu = menuMap.get(menuId);
            if (menu == null) {
                continue;
            }
            stat.setDisplayName(menu.getMenuName());
            stat.setRoutePath(menu.getRoutePath());
            stat.setIcon(menu.getIcon());
        }
        return stats;
    }

    @Override
    public void flush() {
        try {
            Set<String> keys = redisTemplate.keys(SysRedisKeys.API_USAGE_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            // 加载字典：moduleName → 是否匹配菜单
            Map<String, Long> menuIdMap = loadMenuIdMap();

            for (String key : keys) {
                String suffix = key.substring(SysRedisKeys.API_USAGE_PREFIX.length());
                String[] parts = suffix.split(":", 2);
                if (parts.length < 2) {
                    continue;
                }
                Long userId = parseLong(parts[0]);
                String moduleName = parts[1];
                if (userId == null) {
                    continue;
                }

                // 不在字典中 → 非菜单触发的调用，丢弃
                if (!menuIdMap.containsKey(moduleName)) {
                    redisTemplate.delete(key);
                    continue;
                }

                long count = parseLong(hashValue(key, "useCount")) == null ? 0 : parseLong(hashValue(key, "useCount"));
                String apiPath = hashValue(key, "apiPath");
                String lastAccessTime = hashValue(key, "lastAccessTime");
                mapper.upsert(userId, moduleName, apiPath, count,
                        Timestamp.valueOf(hasText(lastAccessTime) ? LocalDateTime.parse(lastAccessTime) : LocalDateTime.now()));
                redisTemplate.delete(key);
            }
        } catch (Exception ex) {
            log.warn("Flush api usage stats failed", ex);
        }
    }

    private Map<String, Long> loadMenuIdMap() {
        return dictDataMapper.selectList(
                        new LambdaQueryWrapper<SysDictData>()
                                .eq(SysDictData::getDictType, DICT_TYPE)
                                .eq(SysDictData::getIsStatus, (short) 1))
                .stream()
                .filter(d -> d.getRemark() != null)
                .collect(Collectors.toMap(
                        SysDictData::getDictValue,
                        d -> parseId(d.getRemark()),
                        (a, b) -> a));
    }

    private Long parseId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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
