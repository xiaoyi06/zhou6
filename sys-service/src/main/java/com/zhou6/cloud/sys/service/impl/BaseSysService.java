package com.zhou6.cloud.sys.service.impl;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;

abstract class BaseSysService {

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
        if (!hasText(value)) {
            return null;
        }
        return parseRequiredId(value, message);
    }

    protected Short toStatus(Integer value) {
        return value == null ? Short.valueOf((short) 1) : value.shortValue();
    }

    protected long pageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    protected long pageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
