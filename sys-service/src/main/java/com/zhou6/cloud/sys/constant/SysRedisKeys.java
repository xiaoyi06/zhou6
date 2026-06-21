package com.zhou6.cloud.sys.constant;

/**
 * 系统服务 Redis Key 约定。
 */
public final class SysRedisKeys {

    public static final String CONFIG_PREFIX = "zhou6:sys:config:";

    public static final String DICT_PREFIX = "zhou6:sys:dict:";

    public static final String WHITELIST_PREFIX = "zhou6:sys:whitelist:";

    public static final String IP_BLACKLIST_PREFIX = "zhou6:sys:ip-blacklist:";

    public static final String TRAFFIC_PV_PREFIX = "zhou6:sys:traffic:pv:";

    public static final String TRAFFIC_UV_PREFIX = "zhou6:sys:traffic:uv:";

    public static final String TRAFFIC_RT_PREFIX = "zhou6:sys:traffic:rt:";

    public static final String MENU_USAGE_PREFIX = "zhou6:sys:menu:usage:";

    public static final String API_USAGE_PREFIX = "zhou6:sys:api:usage:";

    private SysRedisKeys() {
    }
}
