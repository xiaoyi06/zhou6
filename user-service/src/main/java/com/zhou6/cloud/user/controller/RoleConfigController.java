package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.RoleAssignUsersDTO;
import com.zhou6.cloud.user.dto.RoleDataScopeDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleRemoveUserDTO;
import com.zhou6.cloud.user.dto.RoleUserVO;
import com.zhou6.cloud.user.service.RoleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色配置接口，负责角色用户授权和数据权限配置。
 */
@RestController
@RequestMapping("/role/config")
public class RoleConfigController {

    private final RoleService roleService;

    public RoleConfigController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 查询角色下的用户。
     *
     * @param dto 角色 ID 参数
     * @return 角色用户列表
     */
    @PostMapping("/users")
    public R<List<RoleUserVO>> users(@RequestBody RoleIdDTO dto) {
        return R.ok(roleService.users(dto));
    }

    /**
     * 批量给角色分配用户。
     *
     * @param dto 分配用户参数
     * @return 空响应
     */
    @PostMapping("/assignUsers")
    public R<Void> assignUsers(@RequestBody RoleAssignUsersDTO dto) {
        roleService.assignUsers(dto);
        return R.ok(null);
    }

    /**
     * 取消用户角色。
     *
     * @param dto 取消用户参数
     * @return 空响应
     */
    @PostMapping("/removeUser")
    public R<Void> removeUser(@RequestBody RoleRemoveUserDTO dto) {
        roleService.removeUser(dto);
        return R.ok(null);
    }

    /**
     * 配置角色数据权限范围。
     *
     * @param dto 数据权限配置参数
     * @return 空响应
     */
    @PostMapping("/dataScope")
    public R<Void> dataScope(@RequestBody RoleDataScopeDTO dto) {
        roleService.configDataScope(dto);
        return R.ok(null);
    }
}
