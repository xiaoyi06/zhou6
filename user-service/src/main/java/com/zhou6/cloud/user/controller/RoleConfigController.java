package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.RoleAssignUsersDTO;
import com.zhou6.cloud.user.dto.RoleDataScopeDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleRemoveUserDTO;
import com.zhou6.cloud.user.vo.RoleUserVO;
import com.zhou6.cloud.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色配置接口，负责角色用户授权和数据权限配置。
 */
@Tag(name = "角色配置", description = "维护角色用户授权和角色数据权限范围")
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
    @Operation(summary = "查询角色用户", description = "查询指定角色下已授权的用户列表")
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
    @Operation(summary = "给角色分配用户", description = "批量维护用户与角色的授权关系")
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
    @Operation(summary = "取消用户角色", description = "移除指定用户与指定角色的授权关系")
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
    @Operation(summary = "配置角色数据权限", description = "设置角色的数据权限范围和自定义组织机构范围")
    public R<Void> dataScope(@RequestBody RoleDataScopeDTO dto) {
        roleService.configDataScope(dto);
        return R.ok(null);
    }
}
