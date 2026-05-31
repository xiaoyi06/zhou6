package com.zhou6.cloud.user.vo;

import lombok.Data;

/**
 * 角色用户响应对象。
 */
@Data
public class RoleUserVO {

    /** 用户ID。 */
    private String userId;

    /** 登录账号。 */
    private String username;

    /** 用户昵称。 */
    private String nickname;

    /** 联系电话。 */
    private String contactPhone;
}
