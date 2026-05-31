package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.MenuAssignDTO;
import com.zhou6.cloud.user.dto.MenuIdDTO;
import com.zhou6.cloud.user.dto.MenuQueryDTO;
import com.zhou6.cloud.user.dto.MenuSaveDTO;
import com.zhou6.cloud.user.vo.MenuVO;
import com.zhou6.cloud.user.vo.RouterVO;
import com.zhou6.cloud.user.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单权限接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@Tag(name = "菜单权限管理", description = "维护菜单树、角色菜单分配和当前用户路由")
@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 查询菜单树。
     *
     * @param dto 查询条件
     * @return 菜单树
     */
    @PostMapping("/getTree")
    @Operation(summary = "查询菜单树", description = "按条件查询菜单权限并组装为树结构")
    public R<List<MenuVO>> getTree(@RequestBody MenuQueryDTO dto) {
        return R.ok(menuService.getTree(dto));
    }

    /**
     * 新增菜单。
     *
     * @param dto 菜单保存参数
     * @return 空响应
     */
    @PostMapping("/add")
    @Operation(summary = "新增菜单", description = "新增目录、页面或按钮权限")
    public R<Void> add(@RequestBody MenuSaveDTO dto) {
        menuService.add(dto);
        return R.ok(null);
    }

    /**
     * 修改菜单。
     *
     * @param dto 菜单保存参数
     * @return 空响应
     */
    @PostMapping("/edit")
    @Operation(summary = "修改菜单", description = "修改菜单基础信息、路由信息或权限标识")
    public R<Void> edit(@RequestBody MenuSaveDTO dto) {
        menuService.edit(dto);
        return R.ok(null);
    }

    /**
     * 删除菜单。
     *
     * @param dto 菜单 ID 参数
     * @return 空响应
     */
    @PostMapping("/delete")
    @Operation(summary = "删除菜单", description = "删除菜单；存在子菜单或角色关联时不允许删除")
    public R<Void> delete(@RequestBody MenuIdDTO dto) {
        menuService.delete(dto);
        return R.ok(null);
    }

    /**
     * 获取当前用户可见路由。
     *
     * @return 当前用户路由树
     */
    @PostMapping("/getRouters")
    @Operation(summary = "获取当前用户路由", description = "根据当前登录用户权限生成前端路由树")
    public R<List<RouterVO>> getRouters(@RequestBody(required = false) Object ignored) {
        return R.ok(menuService.getRouters());
    }

    /**
     * 给角色分配菜单。
     *
     * @param dto 分配参数
     * @return 空响应
     */
    @PostMapping("/assign")
    @Operation(summary = "给角色分配菜单", description = "维护角色与菜单权限的关联关系")
    public R<Void> assign(@RequestBody MenuAssignDTO dto) {
        menuService.assign(dto);
        return R.ok(null);
    }
}
