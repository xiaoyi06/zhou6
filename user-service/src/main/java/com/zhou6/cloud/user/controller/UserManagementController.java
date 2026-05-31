package com.zhou6.cloud.user.controller;

import java.io.IOException;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.PageResponse;
import com.zhou6.cloud.user.dto.UserChangeStatusDTO;
import com.zhou6.cloud.user.dto.UserDeleteDTO;
import com.zhou6.cloud.user.dto.UserIdDTO;
import com.zhou6.cloud.user.dto.UserManageVO;
import com.zhou6.cloud.user.dto.UserQueryDTO;
import com.zhou6.cloud.user.dto.UserResetPasswordDTO;
import com.zhou6.cloud.user.dto.UserSaveDTO;
import com.zhou6.cloud.user.service.UserManagementService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@RestController
@RequestMapping("/accounts")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * 分页查询用户。
     *
     * @param dto 查询条件
     * @return 用户分页结果
     */
    @PostMapping("/page")
    public R<PageResponse<UserManageVO>> page(@RequestBody UserQueryDTO dto) {
        return R.ok(userManagementService.page(dto));
    }

    /**
     * 新增用户。
     *
     * @param dto 用户保存参数
     * @return 空响应
     */
    @PostMapping("/add")
    public R<Void> add(@RequestBody UserSaveDTO dto) {
        userManagementService.add(dto);
        return R.ok(null);
    }

    /**
     * 修改用户。
     *
     * @param dto 用户保存参数
     * @return 空响应
     */
    @PostMapping("/edit")
    public R<Void> edit(@RequestBody UserSaveDTO dto) {
        userManagementService.edit(dto);
        return R.ok(null);
    }

    /**
     * 删除用户，支持批量删除。
     *
     * @param dto 删除参数
     * @return 空响应
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody UserDeleteDTO dto) {
        userManagementService.delete(dto);
        return R.ok(null);
    }

    /**
     * 修改用户启停状态。
     *
     * @param dto 状态参数
     * @return 空响应
     */
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody UserChangeStatusDTO dto) {
        userManagementService.changeStatus(dto);
        return R.ok(null);
    }

    /**
     * 管理员重置用户密码。
     *
     * @param dto 重置密码参数
     * @return 空响应
     */
    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@RequestBody UserResetPasswordDTO dto) {
        userManagementService.resetPassword(dto);
        return R.ok(null);
    }

    /**
     * 查询用户详情。
     *
     * @param dto 用户 ID 参数
     * @return 用户详情
     */
    @PostMapping("/getById")
    public R<UserManageVO> getById(@RequestBody UserIdDTO dto) {
        return R.ok(userManagementService.getById(dto));
    }

    /**
     * 导出用户列表。
     *
     * @param dto 查询条件
     * @param response 文件响应
     */
    @PostMapping("/export")
    public void export(@RequestBody UserQueryDTO dto, HttpServletResponse response) throws IOException {
        userManagementService.export(dto, response);
    }
}
