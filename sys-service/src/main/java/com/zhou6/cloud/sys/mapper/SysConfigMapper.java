package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置 Mapper，config_value 使用 JSONB，需要显式类型转换。
 */
public interface SysConfigMapper {

    SysConfig selectById(@Param("id") Long id);

    SysConfig selectByKey(@Param("configKey") String configKey);

    List<SysConfig> selectPage(@Param("configKey") String configKey, @Param("configName") String configName,
            @Param("isStatus") Short isStatus, @Param("limit") long limit, @Param("offset") long offset);

    long countPage(@Param("configKey") String configKey, @Param("configName") String configName,
            @Param("isStatus") Short isStatus);

    int insert(SysConfig config);

    int update(SysConfig config);

    int deleteById(@Param("id") Long id);
}
