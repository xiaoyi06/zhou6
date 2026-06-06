package com.zhou6.cloud.sys.mapper;

import java.util.List;

import com.zhou6.cloud.sys.entity.SysConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 系统配置 Mapper，config_value 使用 JSONB，需要显式类型转换。
 */
public interface SysConfigMapper {

    @Select("""
            SELECT id, config_key, config_value::text AS config_value, config_name, is_status, remark, update_time
            FROM sys_config
            WHERE id = #{id}
            """)
    SysConfig selectById(@Param("id") Long id);

    @Select("""
            SELECT id, config_key, config_value::text AS config_value, config_name, is_status, remark, update_time
            FROM sys_config
            WHERE config_key = #{configKey}
            LIMIT 1
            """)
    SysConfig selectByKey(@Param("configKey") String configKey);

    @Select("""
            SELECT id, config_key, config_value::text AS config_value, config_name, is_status, remark, update_time
            FROM sys_config
            WHERE (#{configKey} IS NULL OR config_key LIKE CONCAT('%', #{configKey}, '%'))
              AND (#{configName} IS NULL OR config_name LIKE CONCAT('%', #{configName}, '%'))
              AND (#{isStatus} IS NULL OR is_status = #{isStatus})
            ORDER BY update_time DESC, id DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<SysConfig> selectPage(@Param("configKey") String configKey, @Param("configName") String configName,
            @Param("isStatus") Short isStatus, @Param("limit") long limit, @Param("offset") long offset);

    @Select("""
            SELECT COUNT(1)
            FROM sys_config
            WHERE (#{configKey} IS NULL OR config_key LIKE CONCAT('%', #{configKey}, '%'))
              AND (#{configName} IS NULL OR config_name LIKE CONCAT('%', #{configName}, '%'))
              AND (#{isStatus} IS NULL OR is_status = #{isStatus})
            """)
    long countPage(@Param("configKey") String configKey, @Param("configName") String configName,
            @Param("isStatus") Short isStatus);

    @Insert("""
            INSERT INTO sys_config (id, config_key, config_value, config_name, is_status, remark, update_time)
            VALUES (#{id}, #{configKey}, CAST(#{configValue} AS jsonb), #{configName}, #{isStatus}, #{remark}, CURRENT_TIMESTAMP)
            """)
    int insert(SysConfig config);

    @Update("""
            UPDATE sys_config
            SET config_key = #{configKey},
                config_value = CAST(#{configValue} AS jsonb),
                config_name = #{configName},
                is_status = #{isStatus},
                remark = #{remark},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int update(SysConfig config);

    @Delete("DELETE FROM sys_config WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
