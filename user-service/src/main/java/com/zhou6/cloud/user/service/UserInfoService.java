package com.zhou6.cloud.user.service;

import com.zhou6.cloud.user.vo.UserInfoResponse;
import com.zhou6.cloud.user.dto.UserAvatarDTO;
import com.zhou6.cloud.user.dto.UserChangePasswordDTO;
import com.zhou6.cloud.user.vo.VerifyResponse;

public interface UserInfoService {

    /**
     * 根据数据库用户表校验用户账号密码。
     *
     * @param username 用户账号
     * @param password 用户密码
     * @param loginIp  登录IP
     * @return 用户校验结果
     */
    VerifyResponse verify(String username, String password, String loginIp);

    /**
     * 查询当前登录人的数据库用户基础信息。
     *
     * @return 用户基础信息
     */
    UserInfoResponse getCurrentUserInfo();

    /**
     * 使用当前登录上下文中的登录账号查询当前登录人信息。
     *
     * @return 用户基础信息
     */
    UserInfoResponse getCurrentUserInfoByUsername();

    /**
     * 修改当前登录用户头像。
     *
     * @param dto 头像文件参数
     */
    void updateCurrentUserAvatar(UserAvatarDTO dto);

    /**
     * 当前登录用户修改自己的密码。
     *
     * @param dto 修改密码参数
     */
    void changeCurrentUserPassword(UserChangePasswordDTO dto);
}
