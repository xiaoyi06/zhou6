package com.zhou6.cloud.auth.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.auth.dto.VerifyRequest;
import com.zhou6.cloud.auth.vo.VerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务 Feign 客户端，供认证服务校验账号密码。
 */
@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * 调用用户服务内部校验接口，核对账号密码并返回用户 ID。
     *
     * @param request 用户校验参数
     * @return 用户校验结果
     */
    @PostMapping("/user/userInfo/verify")
    R<VerifyResponse> verify(@RequestBody VerifyRequest request);
}
