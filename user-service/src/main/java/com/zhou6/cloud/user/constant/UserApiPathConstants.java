package com.zhou6.cloud.user.constant;

import com.zhou6.cloud.common.constant.ApiPathConstants;

/**
 * user-service API 路径常量。
 */
public final class UserApiPathConstants {

    /** user-service 接口根路径。 */
    public static final String USER = ApiPathConstants.API_V1 + "/user-api";
    public static final String USER_INFO = USER + "/userInfo";
    public static final String USER_ACCOUNTS = USER + "/userManagement";
    public static final String ROLE = USER + "/role";
    public static final String ROLE_CONFIG = USER + "/role/config";
    public static final String POST = USER + "/post";
    public static final String POST_CONFIG = USER + "/post/config";
    public static final String ORGANIZATION = USER + "/organization";
    public static final String ORGANIZATION_CONFIG = USER + "/organization/config";
    public static final String MENU = USER + "/menu";
    public static final String EXTERNAL_SYSTEM = USER + "/externalSystem";

    private UserApiPathConstants() {
    }
}
