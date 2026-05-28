package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 用户管理响应对象。
 */
@Data
public class UserManageVO {

    /** 用户ID。 */
    private String id;

    /** 登录账号。 */
    private String username;

    /** 用户昵称。 */
    private String nickname;

    /** 联系电话。 */
    private String contactPhone;

    /** 邮箱。 */
    private String email;

    /** 性别，0未知，1男，2女。 */
    private Integer gender;

    /** 主部门ID。 */
    private String primaryOrgId;

    /** 主部门名称。 */
    private String primaryOrgName;

    /** 头像文件ID。 */
    private String avatar;

    /** 个性签名。 */
    private String personalSignature;

    /** 工作状态。 */
    private String workStatus;

    /** 账号状态，1正常，0禁用。 */
    private Integer status;
}
