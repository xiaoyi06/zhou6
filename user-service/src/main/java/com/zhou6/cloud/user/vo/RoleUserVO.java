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

    /** 邮箱。 */
    private String email;

    /** 性别，0未知，1男，2女。 */
    private Integer gender;

    /** 主部门ID。 */
    private String primaryOrgId;

    /** 主部门名称。 */
    private String primaryOrgName;

    /** 头像文件ID。 */
    private String avatarFileId;

    /** 个性签名。 */
    private String personalSignature;

    /** 工作状态。 */
    private String workStatus;

    /** 账号状态，1正常，0禁用。 */
    private Integer status;

    /** 最后登录IP。 */
    private String lastLoginIp;

    /** 最后登录时间。 */
    private String lastLoginTime;

    /** 创建时间。 */
    private String createTime;

    /** 修改时间。 */
    private String updateTime;
}
