package com.zhou6.cloud.community.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.constant.CommunityApiPathConstants;
import com.zhou6.cloud.community.dto.TagReserveDTO;
import com.zhou6.cloud.community.service.CommunityTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "交流社区-标签预留", description = "标签能力预留接口，当前仅维护帖子标签关系")
@RestController
@RequestMapping(CommunityApiPathConstants.TAG)
public class CommunityTagController {

    private final CommunityTagService tagService;

    public CommunityTagController(CommunityTagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/reservePostTags")
    @Operation(summary = "预留帖子标签关系", description = "为后续标签推荐和用户兴趣画像扩展预留")
    public R<Void> reservePostTags(@RequestBody TagReserveDTO dto) {
        tagService.reservePostTags(dto);
        return R.ok(null);
    }
}
