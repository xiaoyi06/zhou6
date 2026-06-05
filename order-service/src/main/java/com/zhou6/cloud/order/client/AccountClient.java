package com.zhou6.cloud.order.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.order.dto.AccountAmountDTO;
import com.zhou6.cloud.order.dto.AccountReverseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 账户服务 Feign 客户端，供订单服务完成现金预冻结与退款冲正。
 */
@FeignClient(name = "account-service")
public interface AccountClient {

    /**
     * 调用账户服务预冻结用户现金。
     *
     * @param request 账户金额变更参数
     * @return true 表示冻结成功
     */
    @PostMapping("/account/api/account/freeze")
    R<Boolean> freeze(@RequestBody AccountAmountDTO request);

    /**
     * 调用账户服务生成红字冲正流水。
     *
     * @param request 红字冲正参数
     * @return 空响应
     */
    @PostMapping("/account/api/account/reverse")
    R<Void> reverse(@RequestBody AccountReverseDTO request);
}
