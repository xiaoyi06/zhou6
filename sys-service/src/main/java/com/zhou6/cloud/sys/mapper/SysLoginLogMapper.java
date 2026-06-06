package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysLoginLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 登录日志查询 Mapper，批量写入由 JdbcTemplate 处理 INET 类型。
 */
public interface SysLoginLogMapper {

    @Select("""
            SELECT user_id, username, ip_address::text AS ip_address, login_location, browser, os, status, msg, login_time
            FROM sys_login_log
            ORDER BY login_time DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<SysLoginLog> selectPage(@Param("limit") long limit, @Param("offset") long offset);

    @Select("SELECT COUNT(1) FROM sys_login_log")
    long count();
}
