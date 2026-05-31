package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.UserAvatarDTO;
import com.zhou6.cloud.user.dto.UserChangePasswordDTO;
import com.zhou6.cloud.user.dto.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyRequest;
import com.zhou6.cloud.user.dto.VerifyResponse;
import com.zhou6.cloud.user.service.UserInfoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userInfo")
public class UserController {

    private final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PostMapping("/verify")
    public R<VerifyResponse> verify(@RequestBody VerifyRequest request,
            @RequestHeader(value = "X-Client-Ip", defaultValue = "unknown") String loginIp) {
        return R.ok(userInfoService.verify(request.getUsername(), request.getPassword(), loginIp));
    }

    @PostMapping("/info")
    public R<UserInfoResponse> info() {
        UserInfoResponse userInfo = userInfoService.getCurrentUserInfo();
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
    public R<Void> changePassword(@RequestBody UserChangePasswordDTO dto) {
        userInfoService.changeCurrentUserPassword(dto);
        return R.ok(null);
    }
}
