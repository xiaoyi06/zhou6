package com.zhou6.cloud.community.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.constant.CommunityApiPathConstants;
import com.zhou6.cloud.community.dto.PageQueryDTO;
import com.zhou6.cloud.community.dto.UserIdDTO;
import com.zhou6.cloud.community.service.CommunityRelationService;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.RelationUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "交流社区-用户关系", description = "关注、喜欢用户和拉黑管理")
@RestController
@RequestMapping(CommunityApiPathConstants.RELATION)
public class CommunityRelationController {

    private final CommunityRelationService relationService;

    public CommunityRelationController(CommunityRelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping("/follow")
    public R<Void> follow(@RequestBody UserIdDTO dto) {
        relationService.follow(dto);
        return R.ok(null);
    }

    @PostMapping("/unfollow")
    public R<Void> unfollow(@RequestBody UserIdDTO dto) {
        relationService.unfollow(dto);
        return R.ok(null);
    }

    @PostMapping("/favoriteUser")
    public R<Void> favoriteUser(@RequestBody UserIdDTO dto) {
        relationService.favoriteUser(dto);
        return R.ok(null);
    }

    @PostMapping("/unfavoriteUser")
    public R<Void> unfavoriteUser(@RequestBody UserIdDTO dto) {
        relationService.unfavoriteUser(dto);
        return R.ok(null);
    }

    @PostMapping("/block")
    @Operation(summary = "拉黑用户", description = "拉黑后信息流屏蔽对方帖子")
    public R<Void> block(@RequestBody UserIdDTO dto) {
        relationService.block(dto);
        return R.ok(null);
    }

    @PostMapping("/unblock")
    public R<Void> unblock(@RequestBody UserIdDTO dto) {
        relationService.unblock(dto);
        return R.ok(null);
    }

    @PostMapping("/followPage")
    public R<PageResponse<RelationUserVO>> followPage(@RequestBody PageQueryDTO dto) {
        return R.ok(relationService.followPage(dto));
    }

    @PostMapping("/favoriteUserPage")
    public R<PageResponse<RelationUserVO>> favoriteUserPage(@RequestBody PageQueryDTO dto) {
        return R.ok(relationService.favoriteUserPage(dto));
    }

    @PostMapping("/blockPage")
    public R<PageResponse<RelationUserVO>> blockPage(@RequestBody PageQueryDTO dto) {
        return R.ok(relationService.blockPage(dto));
    }
}
