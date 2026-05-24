package com.zhou6.cloud.user.service;

import com.zhou6.cloud.user.dto.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyResponse;

public interface UserInfoService {

    /**
     * 根据数据库用户表校验用户账号密码。
     *
     * @param username 用户账号
     * @param password 用户密码
     * @return 用户校验结果
     */
    VerifyResponse verify(String username, String password);

    /**
     * 查询当前登录人的数据库用户基础信息。
     *
     * @return 用户基础信息
     */
    UserInfoResponse getCurrentUserInfo();
}
