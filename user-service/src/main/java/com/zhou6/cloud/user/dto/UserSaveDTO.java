package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 用户新增和修改请求参数。
 */
@Data
public class UserSaveDTO {

    /** 用户ID，新增时为空，修改时必填。 */
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

    /** 登录密码，新增时为空则使用默认密码。 */
    private String password;

    /** 主部门ID。 */
    private String primaryOrgId;

    /** 头像文件ID。 */
    private String avatar = "xxx.fileid";

    /** 个性签名。 */
    private String personalSignature;

    /** 工作状态，取值可来自字典表。 */
    private String workStatus;

    /** 账号状态，1正常，0禁用。 */
    private Integer status;
}
