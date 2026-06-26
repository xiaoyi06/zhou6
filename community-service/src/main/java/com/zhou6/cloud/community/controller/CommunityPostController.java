package com.zhou6.cloud.community.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.constant.CommunityApiPathConstants;
import com.zhou6.cloud.community.dto.IdDTO;
import com.zhou6.cloud.community.dto.PostAuditDTO;
import com.zhou6.cloud.community.dto.PostPageQueryDTO;
import com.zhou6.cloud.community.dto.PostSaveDTO;
import com.zhou6.cloud.community.service.CommunityPostService;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "交流社区-帖子", description = "帖子发布、列表、热门、点赞收藏、删除恢复、置顶和加精")
@RestController
@RequestMapping(CommunityApiPathConstants.POST)
public class CommunityPostController {

    private final CommunityPostService postService;

    public CommunityPostController(CommunityPostService postService) {
        this.postService = postService;
    }

    @PostMapping("/add")
    @Operation(summary = "发布帖子", description = "当前版本直接发布，保留审核状态字段便于后续接入审核流")
    public R<String> add(@RequestBody PostSaveDTO dto) {
        return R.ok(postService.add(dto));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询帖子", description = "scope 支持 ALL、FOLLOWING、FAVORITE_USER、MINE、FAVORITED_POST")
    public R<PageResponse<PostVO>> page(@RequestBody PostPageQueryDTO dto) {
        return R.ok(postService.page(dto));
    }

    @PostMapping("/hot")
    @Operation(summary = "查询热门帖子", description = "按定时刷新后的 heatScore 倒序返回")
    public R<PageResponse<PostVO>> hot(@RequestBody PostPageQueryDTO dto) {
        return R.ok(postService.hot(dto));
    }

    @PostMapping("/detail")
    @Operation(summary = "查询帖子详情")
    public R<PostVO> detail(@RequestBody IdDTO dto) {
        return R.ok(postService.detail(dto));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除帖子", description = "软删除，后续可恢复")
    public R<Void> delete(@RequestBody IdDTO dto) {
        postService.delete(dto);
        return R.ok(null);
    }

    @PostMapping("/restore")
    @Operation(summary = "恢复帖子", description = "管理员恢复已软删除帖子")
    public R<Void> restore(@RequestBody IdDTO dto) {
        postService.restore(dto);
        return R.ok(null);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核帖子", description = "审核功能预留入口；当前发帖默认 APPROVED，后续可接审核流")
    public R<Void> audit(@RequestBody PostAuditDTO dto) {
        postService.audit(dto);
        return R.ok(null);
    }

    @PostMapping("/top")
    @Operation(summary = "置顶帖子")
    public R<Void> top(@RequestBody IdDTO dto) {
        postService.top(dto);
        return R.ok(null);
    }

    @PostMapping("/untop")
    @Operation(summary = "取消置顶")
    public R<Void> untop(@RequestBody IdDTO dto) {
        postService.untop(dto);
        return R.ok(null);
    }

    @PostMapping("/feature")
    @Operation(summary = "加精帖子")
    public R<Void> feature(@RequestBody IdDTO dto) {
        postService.feature(dto);
        return R.ok(null);
    }

    @PostMapping("/unfeature")
    @Operation(summary = "取消加精")
    public R<Void> unfeature(@RequestBody IdDTO dto) {
        postService.unfeature(dto);
        return R.ok(null);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞帖子")
    public R<Void> like(@RequestBody IdDTO dto) {
        postService.like(dto);
        return R.ok(null);
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞帖子")
    public R<Void> unlike(@RequestBody IdDTO dto) {
        postService.unlike(dto);
        return R.ok(null);
    }

    @PostMapping("/favorite")
    @Operation(summary = "收藏帖子")
    public R<Void> favorite(@RequestBody IdDTO dto) {
        postService.favorite(dto);
        return R.ok(null);
    }

    @PostMapping("/unfavorite")
    @Operation(summary = "取消收藏帖子")
    public R<Void> unfavorite(@RequestBody IdDTO dto) {
        postService.unfavorite(dto);
        return R.ok(null);
    }
}
