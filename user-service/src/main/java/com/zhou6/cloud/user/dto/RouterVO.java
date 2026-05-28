package com.zhou6.cloud.user.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 前端路由响应对象。
 */
@Data
public class RouterVO {

    /** 菜单ID。 */
    private String id;

    /** 父菜单ID，用于后端组装路由树。 */
    private String parentId;

    /** 路由名称。 */
    private String name;

    /** 路由地址。 */
    private String path;

    /** 组件路径。 */
    private String component;

    /** 菜单图标。 */
    private String icon;

    /** 是否隐藏。 */
    private Boolean hidden;

    /** 权限标识集合。 */
    private List<String> perms = new ArrayList<>();

    /** 排序号。 */
    private Integer sortOrder;

    /** 子路由。 */
    private List<RouterVO> children = new ArrayList<>();
}
