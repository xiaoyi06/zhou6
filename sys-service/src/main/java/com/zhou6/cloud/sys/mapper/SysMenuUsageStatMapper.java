package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysMenuUsageStat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 常用菜单统计查询 Mapper，批量写入由 JdbcTemplate 合并处理。
 */
public interface SysMenuUsageStatMapper {

    @Select("""
            SELECT user_id, menu_id, menu_name, route_path, use_count, last_use_time
            FROM sys_menu_usage_stat
            WHERE user_id = #{userId}
            ORDER BY use_count DESC, last_use_time DESC
            LIMIT #{limit}
            """)
    List<SysMenuUsageStat> selectFrequent(@Param("userId") Long userId, @Param("limit") int limit);
}
