package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.constant.UserApiPathConstants;
import com.zhou6.cloud.user.dto.UserAvatarDTO;
import com.zhou6.cloud.user.dto.UserChangePasswordDTO;
import com.zhou6.cloud.user.vo.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyRequest;
import com.zhou6.cloud.user.vo.VerifyResponse;
import com.zhou6.cloud.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 当前用户接口，负责登录校验、当前用户资料和个人安全设置。
 */
@Tag(name = "当前用户", description = "提供登录校验、当前用户信息、头像和密码修改接口")
@RestController
@RequestMapping(UserApiPathConstants.USER_INFO)
public class UserController {

    private final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PostMapping("/verify")
    @Operation(summary = "校验登录账号密码", description = "校验用户名和密码，并返回当前用户会话所需基础信息")
    public R<VerifyResponse> verify(@RequestBody VerifyRequest request,
            @RequestHeader(value = "X-Client-Ip", defaultValue = "unknown") String loginIp) {
        if (request == null) {
            return R.ok(new VerifyResponse(false, "", "", "", "", "",
                    CommonErrorCode.LOGIN_FAILED.getMessage()));
        }
        return R.ok(userInfoService.verify(request.getUsername(), request.getPassword(), loginIp));
    }

    @PostMapping("/info")
    @Operation(summary = "查询当前用户信息", description = "根据当前登录上下文查询用户资料、角色和权限")
    public R<UserInfoResponse> info() {
        UserInfoResponse userInfo = userInfoService.getCurrentUserInfo();
        if (userInfo == null) {
            throw new BizException(CommonErrorCode.USER_NOT_FOUND);
        }
        return R.ok(userInfo);
    }

    @PostMapping("/infoByUsername")
    @Operation(summary = "按登录账号查询当前用户信息", description = "使用当前登录上下文中的登录账号查询当前用户资料")
    public R<UserInfoResponse> infoByUsername() {
        UserInfoResponse userInfo = userInfoService.getCurrentUserInfoByUsername();
        if (userInfo == null) {
            throw new BizException(CommonErrorCode.USER_NOT_FOUND);
        }
        return R.ok(userInfo);
    }

    /**
     * 修改当前登录用户头像。头像文件必须先通过 file-service 上传，并传入上传返回的文件 ID。
     *
     * @param dto 头像文件参数
     * @return 空响应
     */
    @PostMapping("/avatar")
    @Operation(summary = "修改当前用户头像", description = "使用 file-service 已上传文件ID更新当前用户头像")
    public R<Void> updateAvatar(@RequestBody UserAvatarDTO dto) {
        userInfoService.updateCurrentUserAvatar(dto);
        return R.ok(null);
    }

    /**
     * 当前登录用户修改自己的密码，需要校验旧密码。
     *
     * @param dto 修改密码参数
     * @return 空响应
     */
    @PostMapping("/changePassword")
    @Operation(summary = "修改当前用户密码", description = "当前登录用户校验旧密码后修改自己的登录密码")
    public R<Void> changePassword(@RequestBody UserChangePasswordDTO dto) {
        userInfoService.changeCurrentUserPassword(dto);
        return R.ok(null);
    }
}
