package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 角色用户分页查询请求参数。
 */
@Data
public class RoleUserQueryDTO {

    /** 角色ID，兼容原 RoleIdDTO 入参字段。 */
    private String id;

    /** 角色ID。 */
    private String roleId;

    /** 用户名称或登录账号，支持右匹配查询。 */
    private String keyword;

    /** 登录账号，支持右匹配查询。 */
    private String username;

    /** 用户昵称，支持右匹配查询。 */
    private String nickname;

    /** 联系电话，支持右匹配查询。 */
    private String contactPhone;

    /** 邮箱，支持右匹配查询。 */
    private String email;

    /** 账号状态，1正常，0禁用。 */
    private Integer status;

    /** 当前页码。 */
    private Integer pageNum = 1;

    /** 每页条数。 */
    private Integer pageSize = 10;
}
