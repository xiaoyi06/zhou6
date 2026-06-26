package com.zhou6.cloud.community.service.impl;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;

abstract class BaseCommunityService {

    protected void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    protected boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected Long parseRequiredId(String value, String message) {
        require(hasText(value), message);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    protected Long parseNullableId(String value, String message) {
        return hasText(value) ? parseRequiredId(value, message) : null;
    }

    protected long pageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    protected long pageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
