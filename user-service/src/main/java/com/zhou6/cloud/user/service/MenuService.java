package com.zhou6.cloud.user.service;

import java.util.List;

import com.zhou6.cloud.user.dto.MenuAssignDTO;
import com.zhou6.cloud.user.dto.MenuAssignRolesDTO;
import com.zhou6.cloud.user.dto.MenuIdDTO;
import com.zhou6.cloud.user.dto.MenuQueryDTO;
import com.zhou6.cloud.user.dto.MenuRoleQueryDTO;
import com.zhou6.cloud.user.dto.MenuSaveDTO;
import com.zhou6.cloud.user.dto.UserMenuQueryDTO;
import com.zhou6.cloud.user.vo.MenuVO;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.vo.RoleVO;
import com.zhou6.cloud.user.vo.RouterVO;

/**
 * 菜单权限业务接口，负责菜单维护、路由生成和角色菜单授权。
 */
public interface MenuService {

    /**
     * 查询菜单树。
     *
     * @param dto 查询条件
     * @return 菜单树
     */
    List<MenuVO> getTree(MenuQueryDTO dto);

    /**
     * 查询用户菜单权限树。
     *
     * @param dto 查询参数
     * @return 菜单树
     */
    List<MenuVO> userMenus(UserMenuQueryDTO dto);

    /**
     * 新增菜单。
     *
     * @param dto 菜单保存参数
     */
    void add(MenuSaveDTO dto);

    /**
     * 修改菜单。
     *
     * @param dto 菜单保存参数
     */
    void edit(MenuSaveDTO dto);

    /**
     * 删除菜单。
     *
     * @param dto 菜单 ID 参数
     */
    void delete(MenuIdDTO dto);

    /**
     * 查询当前用户路由树。
     *
     * @return 路由树
     */
    List<RouterVO> getRouters();

    /**
     * 给角色分配菜单。
     *
     * @param dto 分配参数
     */
    void assign(MenuAssignDTO dto);

    /**
     * 查询菜单已配置角色。
     *
     * @param dto 查询参数
     * @return 角色分页结果
     */
    PageResponse<RoleVO> roles(MenuRoleQueryDTO dto);

    /**
     * 查询菜单未配置角色。
     *
     * @param dto 查询参数
     * @return 角色分页结果
     */
    PageResponse<RoleVO> unassignedRoles(MenuRoleQueryDTO dto);

    /**
     * 覆盖菜单角色配置。
     *
     * @param dto 分配参数
     */
    void assignRoles(MenuAssignRolesDTO dto);

    /**
     * 从菜单中批量移除角色配置。
     *
     * @param dto 菜单角色参数
     */
    void removeRoles(MenuAssignRolesDTO dto);
}
