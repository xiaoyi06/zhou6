package com.zhou6.cloud.auth.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.auth.dto.VerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * 调用用户服务内部校验接口，核对账号密码并返回用户 ID。
     *
     * @param username 用户账号
     * @param password 用户密码
     * @return 用户校验结果
     */
    @PostMapping("/userInfo/verify")
    R<VerifyResponse> verify(@RequestParam String username, @RequestParam String password);
}
