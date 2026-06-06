package com.zhou6.cloud.order.client.fallback;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.order.client.AccountClient;
import com.zhou6.cloud.order.dto.AccountAmountDTO;
import com.zhou6.cloud.order.dto.AccountReverseDTO;
import org.springframework.stereotype.Component;

/**
 * account-service 不可用时的订单侧降级响应。
 */
@Component
public class AccountClientFallback implements AccountClient {

    @Override
    public R<Boolean> freeze(AccountAmountDTO request) {
        return R.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), "账户服务暂不可用，请稍后再试");
    }

    @Override
    public R<Void> reverse(AccountReverseDTO request) {
        return R.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), "账户服务暂不可用，请稍后再试");
    }
}
