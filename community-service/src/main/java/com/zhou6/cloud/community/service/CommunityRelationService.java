package com.zhou6.cloud.community.service;

import com.zhou6.cloud.community.dto.PageQueryDTO;
import com.zhou6.cloud.community.dto.UserIdDTO;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.RelationUserVO;

public interface CommunityRelationService {

    void follow(UserIdDTO dto);

    void unfollow(UserIdDTO dto);

    void favoriteUser(UserIdDTO dto);

    void unfavoriteUser(UserIdDTO dto);

    void block(UserIdDTO dto);

    void unblock(UserIdDTO dto);

    PageResponse<RelationUserVO> followPage(PageQueryDTO dto);

    PageResponse<RelationUserVO> favoriteUserPage(PageQueryDTO dto);

    PageResponse<RelationUserVO> blockPage(PageQueryDTO dto);
}
