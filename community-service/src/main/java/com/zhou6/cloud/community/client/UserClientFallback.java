package com.zhou6.cloud.community.client;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.dto.UserSearchDTO;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.UserSearchVO;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public R<PageResponse<UserSearchVO>> page(UserSearchDTO dto) {
        return R.ok(new PageResponse<>());
    }
}
