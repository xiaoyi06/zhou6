package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 管理员重置用户密码请求参数。
 */
@Data
public class UserResetPasswordDTO {

    /** 用户ID。 */
    private String id;
}
