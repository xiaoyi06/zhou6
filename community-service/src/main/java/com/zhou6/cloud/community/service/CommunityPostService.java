package com.zhou6.cloud.community.service;

import com.zhou6.cloud.community.dto.IdDTO;
import com.zhou6.cloud.community.dto.PostAuditDTO;
import com.zhou6.cloud.community.dto.PostPageQueryDTO;
import com.zhou6.cloud.community.dto.PostSaveDTO;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.PostVO;

public interface CommunityPostService {

    String add(PostSaveDTO dto);

    PageResponse<PostVO> page(PostPageQueryDTO dto);

    PageResponse<PostVO> hot(PostPageQueryDTO dto);

    PostVO detail(IdDTO dto);

    void delete(IdDTO dto);

    void restore(IdDTO dto);

    void audit(PostAuditDTO dto);

    void top(IdDTO dto);

    void untop(IdDTO dto);

    void feature(IdDTO dto);

    void unfeature(IdDTO dto);

    void like(IdDTO dto);

    void unlike(IdDTO dto);

    void favorite(IdDTO dto);

    void unfavorite(IdDTO dto);

    void refreshHeat();
}
