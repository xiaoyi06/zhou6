package com.zhou6.cloud.community.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.constant.CommunityApiPathConstants;
import com.zhou6.cloud.community.dto.MentionCandidateQueryDTO;
import com.zhou6.cloud.community.service.CommunityMentionService;
import com.zhou6.cloud.community.vo.MentionCandidateVO;
import com.zhou6.cloud.community.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "交流社区-@", description = "@ 用户候选查询")
@RestController
@RequestMapping(CommunityApiPathConstants.MENTION)
public class CommunityMentionController {

    private final CommunityMentionService mentionService;

    public CommunityMentionController(CommunityMentionService mentionService) {
        this.mentionService = mentionService;
    }

    @PostMapping("/candidates")
    @Operation(summary = "@ 用户候选", description = "keyword 为空时优先关注/喜欢用户；有关键词时模糊检索系统用户并按关系排序")
    public R<PageResponse<MentionCandidateVO>> candidates(@RequestBody MentionCandidateQueryDTO dto) {
        return R.ok(mentionService.candidates(dto));
    }
}
