package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 用户分页和导出查询请求参数。
 */
@Data
public class UserQueryDTO {

    /** 主部门ID，用于按部门筛选用户。 */
    private String primaryOrgId;

    /** 登录账号，支持模糊查询。 */
    private String username;

    /** 用户昵称，支持模糊查询。 */
    private String nickname;

    /** 联系电话，支持模糊查询。 */
    private String contactPhone;

    /** 邮箱，支持模糊查询。 */
    private String email;

    /** 最后登录IP，支持模糊查询。 */
    private String lastLoginIp;

    /** 账号状态，1正常，0禁用。 */
    private Integer status;

    /** 当前页码。 */
    private Integer pageNum = 1;

    /** 每页条数。 */
    private Integer pageSize = 10;
}
