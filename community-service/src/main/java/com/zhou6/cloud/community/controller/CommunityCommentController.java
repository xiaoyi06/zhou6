package com.zhou6.cloud.community.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.constant.CommunityApiPathConstants;
import com.zhou6.cloud.community.dto.CommentSaveDTO;
import com.zhou6.cloud.community.dto.CommentTreeQueryDTO;
import com.zhou6.cloud.community.dto.IdDTO;
import com.zhou6.cloud.community.service.CommunityCommentService;
import com.zhou6.cloud.community.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "交流社区-评论", description = "评论、任意层级回复和评论点赞")
@RestController
@RequestMapping(CommunityApiPathConstants.COMMENT)
public class CommunityCommentController {

    private final CommunityCommentService commentService;

    public CommunityCommentController(CommunityCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    @Operation(summary = "新增评论或回复", description = "parentId 为空表示一级评论，不为空表示回复任意层评论")
    public R<String> add(@RequestBody CommentSaveDTO dto) {
        return R.ok(commentService.add(dto));
    }

    @PostMapping("/tree")
    @Operation(summary = "查询评论树")
    public R<List<CommentVO>> tree(@RequestBody CommentTreeQueryDTO dto) {
        return R.ok(commentService.tree(dto));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除评论")
    public R<Void> delete(@RequestBody IdDTO dto) {
        commentService.delete(dto);
        return R.ok(null);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞评论")
    public R<Void> like(@RequestBody IdDTO dto) {
        commentService.like(dto);
        return R.ok(null);
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞评论")
    public R<Void> unlike(@RequestBody IdDTO dto) {
        commentService.unlike(dto);
        return R.ok(null);
    }
}
