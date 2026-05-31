package com.zhou6.cloud.user.service;

import java.io.IOException;

import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.dto.UserChangeStatusDTO;
import com.zhou6.cloud.user.dto.UserDeleteDTO;
import com.zhou6.cloud.user.dto.UserIdDTO;
import com.zhou6.cloud.user.vo.UserManageVO;
import com.zhou6.cloud.user.dto.UserQueryDTO;
import com.zhou6.cloud.user.dto.UserResetPasswordDTO;
import com.zhou6.cloud.user.dto.UserSaveDTO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户管理业务接口，负责用户基础维护和矩阵组织关系同步。
 */
public interface UserManagementService {

    /**
     * 分页查询用户。
     *
     * @param dto 查询条件
     * @return 用户分页结果
     */
    PageResponse<UserManageVO> page(UserQueryDTO dto);

    /**
     * 新增用户。
     *
     * @param dto 用户保存参数
     */
    void add(UserSaveDTO dto);

    /**
     * 修改用户。
     *
     * @param dto 用户保存参数
     */
    void edit(UserSaveDTO dto);

    /**
     * 删除用户并清理组织关联。
     *
     * @param dto 删除参数
     */
    void delete(UserDeleteDTO dto);

    /**
     * 修改用户启停状态。
     *
     * @param dto 状态参数
     */
    void changeStatus(UserChangeStatusDTO dto);

    /**
     * 管理员重置用户密码。
     *
     * @param dto 重置密码参数
     */
    void resetPassword(UserResetPasswordDTO dto);

    /**
     * 查询用户详情。
     *
     * @param dto 用户 ID 参数
     * @return 用户详情
     */
    UserManageVO getById(UserIdDTO dto);

    /**
     * 按查询条件导出用户列表。
     *
     * @param dto 查询条件
     * @param response 文件响应
     */
    void export(UserQueryDTO dto, HttpServletResponse response) throws IOException;
}
