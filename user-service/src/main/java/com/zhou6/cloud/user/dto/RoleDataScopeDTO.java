package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 角色数据权限配置请求参数。
 */
@Data
public class RoleDataScopeDTO {

    /** 角色ID。 */
    private String roleId;

    /** 数据权限范围，1全部，2自定义，3本部门，4本部门及以下，5仅本人。 */
    private Integer dataScope;

    /** 自定义数据权限机构ID列表，仅 dataScope=2 时使用。 */
    private List<String> orgIds;
}
