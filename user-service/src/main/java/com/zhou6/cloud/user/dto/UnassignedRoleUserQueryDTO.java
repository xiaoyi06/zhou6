package com.zhou6.cloud.user.dto;

import lombok.Data;

/** 查询未分配给指定角色的用户参数。 */
@Data
public class UnassignedRoleUserQueryDTO {

    /** 需要排除已分配用户的角色ID。 */
    private String excludeRoleId;

    /** 登录账号，支持右匹配查询。 */
    private String username;

    /** 用户昵称，支持右匹配查询。 */
    private String nickname;

    /** 当前页码。 */
    private Integer pageNum = 1;

    /** 每页条数。 */
    private Integer pageSize = 10;
}
