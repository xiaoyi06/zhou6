package com.zhou6.cloud.community.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.dto.UserSearchDTO;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.UserSearchVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    @PostMapping("/api/v1/user-api/userManagement/page")
    R<PageResponse<UserSearchVO>> page(@RequestBody UserSearchDTO dto);
}
