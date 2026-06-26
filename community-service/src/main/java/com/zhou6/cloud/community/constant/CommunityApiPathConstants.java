package com.zhou6.cloud.community.constant;

import com.zhou6.cloud.common.constant.ApiPathConstants;

public final class CommunityApiPathConstants {

    public static final String COMMUNITY_API = ApiPathConstants.API_V1 + "/community-api";
    public static final String POST = COMMUNITY_API + "/post";
    public static final String COMMENT = COMMUNITY_API + "/comment";
    public static final String RELATION = COMMUNITY_API + "/relation";
    public static final String MENTION = COMMUNITY_API + "/mention";
    public static final String TAG = COMMUNITY_API + "/tag";

    private CommunityApiPathConstants() {
    }
}
