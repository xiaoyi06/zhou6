package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysApiUsageStat;
import org.apache.ibatis.annotations.Param;

/**
 * API 调用统计查询 Mapper，批量写入由 JdbcTemplate 合并处理。
 */
public interface SysApiUsageStatMapper {

    List<SysApiUsageStat> selectFrequent(@Param("userId") Long userId, @Param("limit") int limit);

    int upsert(@Param("userId") Long userId, @Param("moduleName") String moduleName,
            @Param("apiPath") String apiPath, @Param("useCount") long useCount,
            @Param("lastAccessTime") java.sql.Timestamp lastAccessTime);
}
