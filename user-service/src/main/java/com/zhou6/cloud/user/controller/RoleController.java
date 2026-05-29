package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.PageResponse;
import com.zhou6.cloud.user.dto.RoleChangeStatusDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleQueryDTO;
import com.zhou6.cloud.user.dto.RoleSaveDTO;
import com.zhou6.cloud.user.dto.RoleVO;
import com.zhou6.cloud.user.service.RoleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
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
    public R<Void> changeStatus(@RequestBody RoleChangeStatusDTO dto) {
        roleService.changeStatus(dto);
        return R.ok(null);
    }
}
