package com.zhou6.cloud.sys.mapper;

import java.util.List;
import java.time.LocalDateTime;

import com.zhou6.cloud.sys.dto.LoginLogDeleteParam;
import com.zhou6.cloud.sys.entity.SysLoginLog;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志查询 Mapper，批量写入由 JdbcTemplate 处理 INET 类型。
 */
public interface SysLoginLogMapper {

    int insertBatch(@Param("logs") List<SysLoginLog> logs);

    List<SysLoginLog> selectPage(@Param("username") String username, @Param("status") Integer status,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime,
            @Param("limit") long limit, @Param("offset") long offset);

    long count(@Param("username") String username, @Param("status") Integer status,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    int deleteByIdAndLoginTime(@Param("id") Long id, @Param("loginTime") LocalDateTime loginTime);

    int deleteByRecords(@Param("records") List<LoginLogDeleteParam> records);
}
