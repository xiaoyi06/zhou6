package com.zhou6.cloud.user.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.user.entity.SysRoleMenu;
import com.zhou6.cloud.user.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;

/**
 * 角色菜单关系服务，统一处理 sys_role_menu 关系表的删除动作。
 */
@Service
public class RoleMenuRelationService {

    private final SysRoleMenuMapper roleMenuMapper;

    public RoleMenuRelationService(SysRoleMenuMapper roleMenuMapper) {
        this.roleMenuMapper = roleMenuMapper;
    }

    /**
     * 删除指定角色和菜单之间的授权关系。
     *
     * @param roleIds 角色ID列表
     * @param menuIds 菜单ID列表
     */
    public void removeRoleMenus(List<Long> roleIds, List<Long> menuIds) {
        if (roleIds == null || roleIds.isEmpty() || menuIds == null || menuIds.isEmpty()) {
            return;
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds)
                .in(SysRoleMenu::getMenuId, menuIds));
    }
}
