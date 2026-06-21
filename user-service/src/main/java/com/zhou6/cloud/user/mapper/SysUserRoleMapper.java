package com.zhou6.cloud.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.user.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联 Mapper。
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    int insertBatch(@Param("relations") List<SysUserRole> relations);
}
