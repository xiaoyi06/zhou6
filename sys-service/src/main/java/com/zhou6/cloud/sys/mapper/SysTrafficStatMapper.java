package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysTrafficStat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 流量统计查询 Mapper，批量写入由 JdbcTemplate 合并处理。
 */
public interface SysTrafficStatMapper {

    @Select("""
            SELECT api_route, pv, uv, avg_rt, stat_time
            FROM sys_traffic_stat
            WHERE (#{apiRoute} IS NULL OR api_route LIKE CONCAT('%', #{apiRoute}, '%'))
            ORDER BY stat_time DESC, api_route ASC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<SysTrafficStat> selectPage(@Param("apiRoute") String apiRoute, @Param("limit") long limit,
            @Param("offset") long offset);

    @Select("""
            SELECT COUNT(1)
            FROM sys_traffic_stat
            WHERE (#{apiRoute} IS NULL OR api_route LIKE CONCAT('%', #{apiRoute}, '%'))
            """)
    long countPage(@Param("apiRoute") String apiRoute);
}
