package com.zhou6.cloud.user.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 菜单树响应对象。
 */
@Data
public class MenuVO {

    /** 菜单ID。 */
    private String id;

    /** 菜单名称。 */
    private String menuName;

    /** 父菜单ID。 */
    private String parentId;

    /** 排序号。 */
    private Integer sortOrder;

    /** 路由地址。 */
    private String routePath;

    /** 组件路径。 */
    private String componentPath;

    /** 菜单类型。 */
    private String menuType;

    /** 权限标识。 */
    private String perms;

    /** 菜单图标。 */
    private String icon;

    /** 显示状态。 */
    private Integer visible;

    /** 菜单状态。 */
    private Integer status;

    /** 子菜单。 */
    private List<MenuVO> children = new ArrayList<>();
}
