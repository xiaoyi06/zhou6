package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 当前登录用户修改自己密码请求参数。
 */
@Data
public class UserChangePasswordDTO {

    /** 当前密码，用于校验本人身份。 */
    private String oldPassword;

    /** 新密码。 */
    private String newPassword;
}
