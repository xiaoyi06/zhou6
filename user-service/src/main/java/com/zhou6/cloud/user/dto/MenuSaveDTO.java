package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 菜单新增和修改请求参数。
 */
@Data
public class MenuSaveDTO {

    /** 菜单ID，新增时为空，修改时必填。 */
    private String id;

    /** 菜单名称。 */
    private String menuName;

    /** 父菜单ID。 */
    private String parentId;

    /** 排序号。 */
    private Integer sortOrder = 0;

    /** 路由地址。 */
    private String routePath;

    /** 组件路径。 */
    private String componentPath;

    /** 菜单类型，M目录，C页面，F按钮。 */
    private String menuType;

    /** 权限标识。 */
    private String perms;

    /** 菜单图标。 */
    private String icon;

    /** 显示状态，1显示，0隐藏。 */
    private Integer visible;

    /** 菜单状态，1正常，0停用。 */
    private Integer status;
}
