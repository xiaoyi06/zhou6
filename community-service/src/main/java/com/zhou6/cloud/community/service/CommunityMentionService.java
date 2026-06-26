package com.zhou6.cloud.community.service;

import com.zhou6.cloud.community.dto.MentionCandidateQueryDTO;
import com.zhou6.cloud.community.vo.MentionCandidateVO;
import com.zhou6.cloud.community.vo.PageResponse;

public interface CommunityMentionService {

    PageResponse<MentionCandidateVO> candidates(MentionCandidateQueryDTO dto);
}
