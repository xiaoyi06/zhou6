package com.zhou6.cloud.auth.client.fallback;

import com.zhou6.cloud.auth.client.UserClient;
import com.zhou6.cloud.auth.dto.VerifyRequest;
import com.zhou6.cloud.auth.vo.VerifyResponse;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import org.springframework.stereotype.Component;

/**
 * user-service 不可用时的认证降级响应。
 */
@Component
public class UserClientFallback implements UserClient {

    @Override
    public R<VerifyResponse> verify(VerifyRequest request) {
        return R.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), "用户服务暂不可用，请稍后再试");
    }
}
