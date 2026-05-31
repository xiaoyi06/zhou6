package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.dto.RoleChangeStatusDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleQueryDTO;
import com.zhou6.cloud.user.dto.RoleSaveDTO;
import com.zhou6.cloud.user.vo.RoleVO;
import com.zhou6.cloud.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@Tag(name = "角色管理", description = "维护角色、数据权限和角色启停状态")
@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 分页查询角色。
     *
     * @param dto 查询条件
     * @return 角色分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询角色", description = "按角色编码、名称或状态分页查询角色")
    public R<PageResponse<RoleVO>> page(@RequestBody RoleQueryDTO dto) {
        return R.ok(roleService.page(dto));
    }

    /**
     * 新增角色。
     *
     * @param dto 角色保存参数
     * @return 空响应
     */
    @PostMapping("/add")
    @Operation(summary = "新增角色", description = "新增角色并配置基础数据权限")
    public R<Void> add(@RequestBody RoleSaveDTO dto) {
        roleService.add(dto);
        return R.ok(null);
    }

    /**
     * 修改角色。
     *
     * @param dto 角色保存参数
     * @return 空响应
     */
    @PostMapping("/edit")
    @Operation(summary = "修改角色", description = "修改角色名称、排序、备注和数据权限")
    public R<Void> edit(@RequestBody RoleSaveDTO dto) {
        roleService.edit(dto);
        return R.ok(null);
    }

    /**
     * 删除角色。
     *
     * @param dto 角色 ID 参数
     * @return 空响应
     */
    @PostMapping("/delete")
    @Operation(summary = "删除角色", description = "删除角色；存在关联用户时不允许删除")
    public R<Void> delete(@RequestBody RoleIdDTO dto) {
        roleService.delete(dto);
        return R.ok(null);
    }

    /**
     * 修改角色启停状态。
     *
     * @param dto 状态参数
     * @return 空响应
     */
    @PostMapping("/changeStatus")
    @Operation(summary = "修改角色状态", description = "启用或停用指定角色")
    public R<Void> changeStatus(@RequestBody RoleChangeStatusDTO dto) {
        roleService.changeStatus(dto);
        return R.ok(null);
    }
}
