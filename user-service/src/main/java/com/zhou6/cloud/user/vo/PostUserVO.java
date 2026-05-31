package com.zhou6.cloud.user.vo;

import lombok.Data;

/**
 * 岗位配置用户响应对象。
 */
@Data
public class PostUserVO {

    /** 用户ID。 */
    private String userId;

    /** 登录账号。 */
    private String username;

    /** 用户昵称。 */
    private String nickname;

    /** 联系电话。 */
    private String contactPhone;

    /** 部门ID。 */
    private String orgId;

    /** 部门名称。 */
    private String orgName;
}
