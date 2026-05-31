package com.zhou6.cloud.user.service;

import java.util.List;

import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.dto.RoleAssignUsersDTO;
import com.zhou6.cloud.user.dto.RoleChangeStatusDTO;
import com.zhou6.cloud.user.dto.RoleDataScopeDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleQueryDTO;
import com.zhou6.cloud.user.dto.RoleRemoveUserDTO;
import com.zhou6.cloud.user.dto.RoleSaveDTO;
import com.zhou6.cloud.user.vo.RoleUserVO;
import com.zhou6.cloud.user.vo.RoleVO;

/**
 * 角色管理业务接口，负责角色基础维护、用户授权和数据权限配置。
 */
public interface RoleService {

    /**
     * 分页查询角色。
     *
     * @param dto 查询条件
     * @return 角色分页结果
     */
    PageResponse<RoleVO> page(RoleQueryDTO dto);

    /**
     * 新增角色。
     *
     * @param dto 角色保存参数
     */
    void add(RoleSaveDTO dto);

    /**
     * 修改角色。
     *
     * @param dto 角色保存参数
     */
    void edit(RoleSaveDTO dto);

    /**
     * 删除角色。
     *
     * @param dto 角色 ID 参数
     */
    void delete(RoleIdDTO dto);

    /**
     * 修改角色启停状态。
     *
     * @param dto 状态参数
     */
    void changeStatus(RoleChangeStatusDTO dto);

    /**
     * 查询角色下的用户。
     *
     * @param dto 角色 ID 参数
     * @return 角色用户列表
     */
    List<RoleUserVO> users(RoleIdDTO dto);

    /**
     * 批量给角色分配用户。
     *
     * @param dto 分配用户参数
     */
    void assignUsers(RoleAssignUsersDTO dto);

    /**
     * 取消用户角色。
     *
     * @param dto 取消用户参数
     */
    void removeUser(RoleRemoveUserDTO dto);

    /**
     * 配置角色数据权限范围。
     *
     * @param dto 数据权限配置参数
     */
    void configDataScope(RoleDataScopeDTO dto);
}
