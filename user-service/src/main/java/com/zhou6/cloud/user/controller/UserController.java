package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyResponse;
import com.zhou6.cloud.user.service.UserInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userInfo")
public class UserController {

    private final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PostMapping("/verify")
    public R<VerifyResponse> verify(@RequestParam String username, @RequestParam String password) {
        return R.ok(userInfoService.verify(username, password));
    }

    @PostMapping("/info")
    public R<UserInfoResponse> info() {
        UserInfoResponse userInfo = userInfoService.getCurrentUserInfo();
        if (userInfo == null) {
            throw new BizException(CommonErrorCode.USER_NOT_FOUND);
        }
        return R.ok(userInfo);
    }
}
