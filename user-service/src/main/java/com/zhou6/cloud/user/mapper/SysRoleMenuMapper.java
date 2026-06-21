package com.zhou6.cloud.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.user.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单关联 Mapper。
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    int insertBatch(@Param("relations") List<SysRoleMenu> relations);
}
