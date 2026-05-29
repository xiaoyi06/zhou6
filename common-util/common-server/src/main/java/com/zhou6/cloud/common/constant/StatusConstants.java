package com.zhou6.cloud.common.constant;

/**
 * 系统通用状态常量，所有模块统一引用，避免各自定义导致不一致。
 */
public final class StatusConstants {

    private StatusConstants() {
        // 工具类不可实例化
    }

    /** 启用/正常 */
    public static final short STATUS_ENABLED = 1;

    /** 停用/禁用 */
    public static final short STATUS_DISABLED = 0;

    /** 账号未锁定 */
    public static final short LOCKED_NO = 0;

    /** 账号已锁定 */
    public static final short LOCKED_YES = 1;

    /** 非主部门/非主岗位 */
    public static final short PRIMARY_NO = 0;

    /** 主部门/主岗位 */
    public static final short PRIMARY_YES = 1;

    /** 菜单不可见 */
    public static final short VISIBLE_NO = 0;

    /** 菜单可见 */
    public static final short VISIBLE_YES = 1;

    /** 角色数据权限-全部 */
    public static final short DATA_SCOPE_ALL = 1;

    /** 角色数据权限-自定义 */
    public static final short DATA_SCOPE_CUSTOM = 2;

    /**
     * 校验 status 值是否合法（0 或 1）。
     *
     * @param status 待校验的状态值
     * @return true 表示合法
     */
    public static boolean isValidStatus(Short status) {
        return status != null && (status == STATUS_ENABLED || status == STATUS_DISABLED);
    }
}
