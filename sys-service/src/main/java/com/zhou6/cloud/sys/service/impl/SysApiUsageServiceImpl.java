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
import com.zhou6.cloud.sys.dto.MenuUsageSummaryQueryDTO;
import com.zhou6.cloud.sys.dto.MenuUsageUserQueryDTO;
import com.zhou6.cloud.sys.entity.SysApiUsageStat;
import com.zhou6.cloud.sys.entity.SysDictData;
import com.zhou6.cloud.sys.mapper.SysApiUsageStatMapper;
import com.zhou6.cloud.sys.mapper.SysDictDataMapper;
import com.zhou6.cloud.sys.mapper.SysMenuMapper;
import com.zhou6.cloud.sys.mapper.SysMenuMapper.MenuRouteIcon;
import com.zhou6.cloud.sys.service.SysApiUsageService;
import com.zhou6.cloud.sys.vo.ApiUsageStatVO;
import com.zhou6.cloud.sys.vo.MenuUsageSummaryVO;
import com.zhou6.cloud.sys.vo.MenuUsageUserVO;
import com.zhou6.cloud.sys.vo.PageResponse;
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
    public List<ApiUsageStatVO> frequent(MenuUsageQueryDTO dto) {
        require(dto != null, "常用菜单查询参数不能为空");
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 10 : Math.min(dto.getLimit(), 50);

        List<SysApiUsageStat> stats = mapper.selectFrequent(userId, limit);
        if (stats.isEmpty()) {
            return stats.stream().map(this::toVo).toList();
        }

        // 加载字典：moduleName → menuId(remark)
        Map<String, Long> menuIdMap = loadMenuIdMap();
        if (menuIdMap.isEmpty()) {
            return stats.stream().map(this::toVo).toList();
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
        return stats.stream().map(this::toVo).toList();
    }

    @Override
    public PageResponse<MenuUsageSummaryVO> menuPage(MenuUsageSummaryQueryDTO dto) {
        MenuUsageSummaryQueryDTO query = dto == null ? new MenuUsageSummaryQueryDTO() : dto;
        long pageNum = pageNum(query.getPageNum());
        long pageSize = pageSize(query.getPageSize());
        Long userId = parseNullableId(query.getUserId(), "用户ID不正确");
        String moduleName = hasText(query.getModuleName()) ? query.getModuleName() : null;
        List<MenuUsageSummaryVO> records = mapper.selectMenuPage(userId, moduleName, pageSize, (pageNum - 1) * pageSize);
        enrichMenuSummaries(records);
        return new PageResponse<>(mapper.countMenuPage(userId, moduleName), pageNum, pageSize, records);
    }

    @Override
    public PageResponse<MenuUsageUserVO> menuUserPage(MenuUsageUserQueryDTO dto) {
        require(dto != null && hasText(dto.getModuleName()), "菜单模块标识不能为空");
        long pageNum = pageNum(dto.getPageNum());
        long pageSize = pageSize(dto.getPageSize());
        String keyword = hasText(dto.getKeyword()) ? dto.getKeyword() : null;
        return new PageResponse<>(mapper.countMenuUsers(dto.getModuleName(), keyword), pageNum, pageSize,
                mapper.selectMenuUsers(dto.getModuleName(), keyword, pageSize, (pageNum - 1) * pageSize));
    }

    @Override
    public synchronized void flush() {
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

    @Override
    public void sync() {
        flush();
    }

    @Override
    public synchronized void clear() {
        try {
            Set<String> keys = redisTemplate.keys(SysRedisKeys.API_USAGE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            mapper.clearAll();
        } catch (Exception ex) {
            log.warn("Clear api usage stats failed", ex);
            throw new IllegalStateException("清空菜单访问统计失败", ex);
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

    private void enrichMenuSummaries(List<MenuUsageSummaryVO> records) {
        if (records.isEmpty()) {
            return;
        }
        Map<String, Long> menuIdMap = loadMenuIdMap();
        Map<Long, MenuRouteIcon> menuMap = records.stream()
                .map(record -> menuIdMap.get(record.getModuleName()))
                .filter(Objects::nonNull)
                .distinct()
                .map(sysMenuMapper::selectRouteIconById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MenuRouteIcon::getMenuId, menu -> menu));
        for (MenuUsageSummaryVO record : records) {
            MenuRouteIcon menu = menuMap.get(menuIdMap.get(record.getModuleName()));
            if (menu != null) {
                record.setDisplayName(menu.getMenuName());
                record.setRoutePath(menu.getRoutePath());
                record.setIcon(menu.getIcon());
            }
        }
    }

    private Long parseId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ApiUsageStatVO toVo(SysApiUsageStat stat) {
        ApiUsageStatVO vo = new ApiUsageStatVO();
        vo.setUserId(stat.getUserId() == null ? null : String.valueOf(stat.getUserId()));
        vo.setModuleName(stat.getModuleName());
        vo.setDisplayName(stat.getDisplayName());
        vo.setApiPath(stat.getApiPath());
        vo.setRoutePath(stat.getRoutePath());
        vo.setIcon(stat.getIcon());
        vo.setUseCount(stat.getUseCount());
        vo.setLastAccessTime(stat.getLastAccessTime());
        return vo;
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
