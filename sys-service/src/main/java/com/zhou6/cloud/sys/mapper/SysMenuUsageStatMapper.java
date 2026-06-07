package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysMenuUsageStat;
import org.apache.ibatis.annotations.Param;

/**
 * 常用菜单统计查询 Mapper，批量写入由 JdbcTemplate 合并处理。
 */
public interface SysMenuUsageStatMapper {

    List<SysMenuUsageStat> selectFrequent(@Param("userId") Long userId, @Param("limit") int limit);

    int upsert(@Param("userId") Long userId, @Param("menuId") Long menuId,
            @Param("menuName") String menuName, @Param("routePath") String routePath,
            @Param("useCount") long useCount, @Param("lastUseTime") java.sql.Timestamp lastUseTime);
}
