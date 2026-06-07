package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysLoginLog;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志查询 Mapper，批量写入由 JdbcTemplate 处理 INET 类型。
 */
public interface SysLoginLogMapper {

    List<SysLoginLog> selectPage(@Param("limit") long limit, @Param("offset") long offset);

    long count();
}
