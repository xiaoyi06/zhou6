package com.zhou6.cloud.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.user.entity.SysUserOrganization;
import org.apache.ibatis.annotations.Param;

/**
 * 用户组织关联 Mapper。
 */
public interface SysUserOrganizationMapper extends BaseMapper<SysUserOrganization> {

    int insertBatch(@Param("relations") List<SysUserOrganization> relations);

    int upsertBatch(@Param("relations") List<SysUserOrganization> relations);
}
