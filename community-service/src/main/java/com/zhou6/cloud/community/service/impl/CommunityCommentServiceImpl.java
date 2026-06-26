package com.zhou6.cloud.community.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.community.dto.CommentSaveDTO;
import com.zhou6.cloud.community.dto.CommentTreeQueryDTO;
import com.zhou6.cloud.community.dto.IdDTO;
import com.zhou6.cloud.community.entity.CommunityComment;
import com.zhou6.cloud.community.entity.CommunityCommentLike;
import com.zhou6.cloud.community.entity.CommunityMention;
import com.zhou6.cloud.community.mapper.CommunityCommentLikeMapper;
import com.zhou6.cloud.community.mapper.CommunityCommentMapper;
import com.zhou6.cloud.community.mapper.CommunityMentionMapper;
import com.zhou6.cloud.community.mapper.CommunityPostMapper;
import com.zhou6.cloud.community.mq.CommunityInteractionMessage;
import com.zhou6.cloud.community.mq.CommunityInteractionPublisher;
import com.zhou6.cloud.community.service.CommunityInteractionCacheService;
import com.zhou6.cloud.community.service.CommunityCommentService;
import com.zhou6.cloud.community.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityCommentServiceImpl extends BaseCommunityService implements CommunityCommentService {

    private static final String STATUS_NORMAL = "NORMAL";
    private static final String BUSINESS_COMMENT = "COMMENT";

    private final CommunityCommentMapper commentMapper;
    private final CommunityCommentLikeMapper commentLikeMapper;
    private final CommunityMentionMapper mentionMapper;
    private final CommunityPostMapper postMapper;
    private final CommunityPostServiceImpl postService;
    private final CommunityInteractionCacheService interactionCacheService;
    private final CommunityInteractionPublisher interactionPublisher;

    public CommunityCommentServiceImpl(CommunityCommentMapper commentMapper,
            CommunityCommentLikeMapper commentLikeMapper,
            CommunityMentionMapper mentionMapper,
            CommunityPostMapper postMapper,
            CommunityPostServiceImpl postService,
            CommunityInteractionCacheService interactionCacheService,
            CommunityInteractionPublisher interactionPublisher) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.mentionMapper = mentionMapper;
        this.postMapper = postMapper;
        this.postService = postService;
        this.interactionCacheService = interactionCacheService;
        this.interactionPublisher = interactionPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(CommentSaveDTO dto) {
        Long userId = currentUserId();
        validate(dto);
        Long postId = parseRequiredId(dto.getPostId(), "帖子ID不能为空");
        require(postMapper.selectById(postId) != null, "帖子不存在");
        Long parentId = parseNullableId(dto.getParentId(), "父级评论ID不正确");
        Long rootId = null;
        if (parentId != null) {
            CommunityComment parent = requireComment(parentId);
            require(postId.equals(parent.getPostId()), "父级评论不属于当前帖子");
            rootId = parent.getRootId() == null ? parent.getId() : parent.getRootId();
        }
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setRootId(rootId);
        comment.setAuthorUserId(userId);
        comment.setReplyToUserId(parseNullableId(dto.getReplyToUserId(), "回复用户ID不正确"));
        comment.setContent(dto.getContent().trim());
        comment.setStatus(STATUS_NORMAL);
        comment.setLikeCount(0L);
        commentMapper.insert(comment);
        if (comment.getRootId() == null) {
            comment.setRootId(comment.getId());
            commentMapper.update(null, new LambdaUpdateWrapper<CommunityComment>()
                    .eq(CommunityComment::getId, comment.getId())
                    .set(CommunityComment::getRootId, comment.getId()));
        }
        saveMentions(comment.getId(), postId, userId, dto.getMentionUserIds());
        postService.increaseCommentCount(postId);
        return String.valueOf(comment.getId());
    }

    @Override
    public List<CommentVO> tree(CommentTreeQueryDTO dto) {
        Long postId = parseRequiredId(dto == null ? null : dto.getPostId(), "帖子ID不能为空");
        Long userId = UserContextHolder.getUserId();
        List<CommunityComment> comments = commentMapper.selectList(new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getPostId, postId)
                .isNull(CommunityComment::getDeletedAt)
                .orderByAsc(CommunityComment::getCreateTime)
                .orderByAsc(CommunityComment::getId));
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> liked = likedCommentIds(comments.stream().map(CommunityComment::getId).toList(), userId);
        Map<Long, CommentVO> voMap = comments.stream()
                .map(comment -> toVo(comment, liked))
                .collect(Collectors.toMap(vo -> parseRequiredId(vo.getId(), "评论ID不正确"), vo -> vo));
        List<CommentVO> roots = new ArrayList<>();
        for (CommunityComment comment : comments) {
            CommentVO vo = voMap.get(comment.getId());
            if (comment.getParentId() == null) {
                roots.add(vo);
            } else {
                CommentVO parent = voMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "评论ID不能为空");
        CommunityComment comment = requireComment(id);
        Long userId = currentUserId();
        require(userId.equals(comment.getAuthorUserId()), "只能删除自己的评论");
        commentMapper.update(null, new LambdaUpdateWrapper<CommunityComment>()
                .eq(CommunityComment::getId, id)
                .set(CommunityComment::getDeletedAt, java.time.LocalDateTime.now()));
        postService.decreaseCommentCount(comment.getPostId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(IdDTO dto) {
        Long userId = currentUserId();
        Long commentId = parseRequiredId(dto == null ? null : dto.getId(), "评论ID不能为空");
        CommunityComment comment = requireComment(commentId);
        warmCommentLike(commentId, userId, comment);
        CommunityInteractionMessage message = message("LIKE", "COMMENT", commentId, userId);
        if (interactionCacheService.commentLike(commentId, userId, message)) {
            interactionPublisher.publish(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(IdDTO dto) {
        Long userId = currentUserId();
        Long commentId = parseRequiredId(dto == null ? null : dto.getId(), "评论ID不能为空");
        CommunityComment comment = requireComment(commentId);
        warmCommentLike(commentId, userId, comment);
        CommunityInteractionMessage message = message("UNLIKE", "COMMENT", commentId, userId);
        if (interactionCacheService.commentUnlike(commentId, userId, message)) {
            interactionPublisher.publish(message);
        }
    }

    private CommentVO toVo(CommunityComment comment, Set<Long> liked) {
        CommentVO vo = new CommentVO();
        vo.setId(String.valueOf(comment.getId()));
        vo.setPostId(String.valueOf(comment.getPostId()));
        vo.setParentId(comment.getParentId() == null ? null : String.valueOf(comment.getParentId()));
        vo.setRootId(comment.getRootId() == null ? null : String.valueOf(comment.getRootId()));
        vo.setAuthorUserId(String.valueOf(comment.getAuthorUserId()));
        vo.setReplyToUserId(comment.getReplyToUserId() == null ? null : String.valueOf(comment.getReplyToUserId()));
        vo.setContent(comment.getContent());
        vo.setLikeCount(String.valueOf(interactionCacheService.count("COMMENT", "LIKE", comment.getId(), valueOrZero(comment.getLikeCount()))));
        Long userId = UserContextHolder.getUserId();
        vo.setLiked(userId != null && liked.contains(comment.getId()));
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    private Set<Long> likedCommentIds(List<Long> commentIds, Long userId) {
        if (userId == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<CommunityCommentLike>()
                        .eq(CommunityCommentLike::getUserId, userId)
                        .in(CommunityCommentLike::getCommentId, commentIds))
                .stream().map(CommunityCommentLike::getCommentId).collect(Collectors.toSet());
    }

    private void saveMentions(Long commentId, Long postId, Long operatorUserId, List<String> mentionUserIds) {
        if (mentionUserIds == null || mentionUserIds.isEmpty()) {
            return;
        }
        for (Long mentionedUserId : mentionUserIds.stream().filter(this::hasText)
                .map(id -> parseRequiredId(id, "@用户ID不正确"))
                .filter(id -> !id.equals(operatorUserId))
                .distinct().toList()) {
            CommunityMention mention = new CommunityMention();
            mention.setBusinessType(BUSINESS_COMMENT);
            mention.setBusinessId(commentId);
            mention.setPostId(postId);
            mention.setOperatorUserId(operatorUserId);
            mention.setMentionedUserId(mentionedUserId);
            mentionMapper.insert(mention);
        }
    }

    private CommunityComment requireComment(Long id) {
        CommunityComment comment = commentMapper.selectById(id);
        require(comment != null && comment.getDeletedAt() == null, "评论不存在");
        return comment;
    }

    private void warmCommentLike(Long commentId, Long userId, CommunityComment comment) {
        interactionCacheService.initCountIfAbsent("COMMENT", "LIKE", commentId, valueOrZero(comment.getLikeCount()));
        if (commentLikeMapper.selectCount(new LambdaQueryWrapper<CommunityCommentLike>()
                .eq(CommunityCommentLike::getCommentId, commentId)
                .eq(CommunityCommentLike::getUserId, userId)) > 0) {
            interactionCacheService.cacheCommentLiked(commentId, userId);
        }
    }

    private CommunityInteractionMessage message(String action, String targetType, Long targetId, Long userId) {
        CommunityInteractionMessage message = new CommunityInteractionMessage();
        message.setEventId(java.util.UUID.randomUUID().toString());
        message.setAction(action);
        message.setTargetType(targetType);
        message.setTargetId(targetId);
        message.setUserId(userId);
        message.setEventTime(java.time.LocalDateTime.now());
        return message;
    }

    private void validate(CommentSaveDTO dto) {
        require(dto != null, "评论参数不能为空");
        require(hasText(dto.getPostId()), "帖子ID不能为空");
        require(hasText(dto.getContent()), "评论内容不能为空");
        require(dto.getContent().trim().length() <= 5000, "评论内容不能超过5000个字符");
    }

    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        require(userId != null && userId > 0, "当前用户不存在");
        return userId;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}
