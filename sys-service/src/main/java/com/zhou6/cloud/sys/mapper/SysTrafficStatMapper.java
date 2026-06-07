package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysTrafficStat;
import org.apache.ibatis.annotations.Param;

/**
 * 流量统计查询 Mapper，批量写入由 JdbcTemplate 合并处理。
 */
public interface SysTrafficStatMapper {

    List<SysTrafficStat> selectPage(@Param("apiRoute") String apiRoute, @Param("limit") long limit,
            @Param("offset") long offset);

    long countPage(@Param("apiRoute") String apiRoute);

    int upsert(@Param("apiRoute") String apiRoute, @Param("pv") long pv, @Param("uv") long uv,
            @Param("avgRt") int avgRt, @Param("statTime") java.sql.Timestamp statTime);
}
