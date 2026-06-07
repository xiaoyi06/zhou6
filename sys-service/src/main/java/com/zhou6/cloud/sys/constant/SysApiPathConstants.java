package com.zhou6.cloud.sys.constant;

import com.zhou6.cloud.common.constant.ApiPathConstants;

/**
 * 系统服务接口路径常量。
 */
public final class SysApiPathConstants {

    public static final String SYS = ApiPathConstants.API_V1 + "/sys-api";

    public static final String DICT = SYS + "/dict";

    public static final String CONFIG = SYS + "/config";

    public static final String WHITELIST = SYS + "/whitelist";

    public static final String AUDIT = SYS + "/audit";

    public static final String TRAFFIC = SYS + "/traffic";

    public static final String MENU_USAGE = SYS + "/menuUsage";

    public static final String API_USAGE = SYS + "/apiUsage";

    private SysApiPathConstants() {
    }
}
