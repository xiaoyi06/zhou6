package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 角色新增和修改请求参数。
 */
@Data
public class RoleSaveDTO {

    /** 角色ID，新增时为空，修改时必填。 */
    private String id;

    /** 角色名称。 */
    private String roleName;

    /** 角色编码，新增时必填且全局唯一，修改时忽略。 */
    private String roleCode;

    /** 数据权限范围，1全部，2自定义，3本部门，4本部门及以下，5仅本人。 */
    private Integer dataScope;

    /** 排序号。 */
    private Integer sortOrder = 0;

    /** 备注。 */
    private String remark;
}
