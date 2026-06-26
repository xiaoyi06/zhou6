package com.zhou6.cloud.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhou6.cloud.community.entity.CommunityComment;
import com.zhou6.cloud.community.entity.CommunityCommentLike;
import com.zhou6.cloud.community.entity.CommunityFavorite;
import com.zhou6.cloud.community.entity.CommunityPost;
import com.zhou6.cloud.community.entity.CommunityPostLike;
import com.zhou6.cloud.community.mapper.CommunityCommentLikeMapper;
import com.zhou6.cloud.community.mapper.CommunityCommentMapper;
import com.zhou6.cloud.community.mapper.CommunityFavoriteMapper;
import com.zhou6.cloud.community.mapper.CommunityPostLikeMapper;
import com.zhou6.cloud.community.mapper.CommunityPostMapper;
import com.zhou6.cloud.community.mq.CommunityInteractionMessage;
import com.zhou6.cloud.community.mq.CommunityInteractionPublisher;
import com.zhou6.cloud.community.service.CommunityInteractionCacheService;
import com.zhou6.cloud.community.service.CommunityInteractionPersistenceService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityInteractionPersistenceServiceImpl extends BaseCommunityService
        implements CommunityInteractionPersistenceService {

    private static final String POST = "POST";
    private static final String COMMENT = "COMMENT";
    private static final String LIKE = "LIKE";
    private static final String UNLIKE = "UNLIKE";
    private static final String FAVORITE = "FAVORITE";
    private static final String UNFAVORITE = "UNFAVORITE";
    private static final String VIEW = "VIEW";

    private final CommunityPostLikeMapper postLikeMapper;
    private final CommunityFavoriteMapper favoriteMapper;
    private final CommunityCommentLikeMapper commentLikeMapper;
    private final CommunityPostMapper postMapper;
    private final CommunityCommentMapper commentMapper;
    private final CommunityInteractionCacheService cacheService;
    private final CommunityInteractionPublisher publisher;

    public CommunityInteractionPersistenceServiceImpl(CommunityPostLikeMapper postLikeMapper,
            CommunityFavoriteMapper favoriteMapper,
            CommunityCommentLikeMapper commentLikeMapper,
            CommunityPostMapper postMapper,
            CommunityCommentMapper commentMapper,
            CommunityInteractionCacheService cacheService,
            CommunityInteractionPublisher publisher) {
        this.postLikeMapper = postLikeMapper;
        this.favoriteMapper = favoriteMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.cacheService = cacheService;
        this.publisher = publisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persist(CommunityInteractionMessage message) {
        require(message != null, "互动事件不能为空");
        String action = normalize(message.getAction());
        String targetType = normalize(message.getTargetType());
        if (POST.equals(targetType)) {
            persistPostEvent(action, message);
        } else if (COMMENT.equals(targetType)) {
            persistCommentEvent(action, message);
        } else {
            require(false, "互动对象类型不正确");
        }
        cacheService.removePending(publisher.toEventJson(message));
    }

    private void persistPostEvent(String action, CommunityInteractionMessage message) {
        if (LIKE.equals(action)) {
            CommunityPostLike like = new CommunityPostLike();
            like.setPostId(message.getTargetId());
            like.setUserId(message.getUserId());
            try {
                postLikeMapper.insert(like);
            } catch (DuplicateKeyException ignored) {
                // 幂等消费：同一用户对同一帖子只能有一条点赞关系。
            }
            refreshPostLikeCount(message.getTargetId());
            return;
        }
        if (UNLIKE.equals(action)) {
            postLikeMapper.delete(new LambdaQueryWrapper<CommunityPostLike>()
                    .eq(CommunityPostLike::getPostId, message.getTargetId())
                    .eq(CommunityPostLike::getUserId, message.getUserId()));
            refreshPostLikeCount(message.getTargetId());
            return;
        }
        if (FAVORITE.equals(action)) {
            CommunityFavorite favorite = new CommunityFavorite();
            favorite.setPostId(message.getTargetId());
            favorite.setUserId(message.getUserId());
            try {
                favoriteMapper.insert(favorite);
            } catch (DuplicateKeyException ignored) {
                // 幂等消费。
            }
            refreshPostFavoriteCount(message.getTargetId());
            return;
        }
        if (UNFAVORITE.equals(action)) {
            favoriteMapper.delete(new LambdaQueryWrapper<CommunityFavorite>()
                    .eq(CommunityFavorite::getPostId, message.getTargetId())
                    .eq(CommunityFavorite::getUserId, message.getUserId()));
            refreshPostFavoriteCount(message.getTargetId());
            return;
        }
        if (VIEW.equals(action)) {
            postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, message.getTargetId())
                    .set(CommunityPost::getViewCount, cacheService.count(POST, VIEW, message.getTargetId(), 0L)));
        }
    }

    private void persistCommentEvent(String action, CommunityInteractionMessage message) {
        if (LIKE.equals(action)) {
            CommunityCommentLike like = new CommunityCommentLike();
            like.setCommentId(message.getTargetId());
            like.setUserId(message.getUserId());
            try {
                commentLikeMapper.insert(like);
            } catch (DuplicateKeyException ignored) {
                // 幂等消费。
            }
            refreshCommentLikeCount(message.getTargetId());
            return;
        }
        if (UNLIKE.equals(action)) {
            commentLikeMapper.delete(new LambdaQueryWrapper<CommunityCommentLike>()
                    .eq(CommunityCommentLike::getCommentId, message.getTargetId())
                    .eq(CommunityCommentLike::getUserId, message.getUserId()));
            refreshCommentLikeCount(message.getTargetId());
        }
    }

    private void refreshPostLikeCount(Long postId) {
        Long count = postLikeMapper.selectCount(new LambdaQueryWrapper<CommunityPostLike>().eq(CommunityPostLike::getPostId, postId));
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>().eq(CommunityPost::getId, postId)
                .set(CommunityPost::getLikeCount, count == null ? 0L : count));
    }

    private void refreshPostFavoriteCount(Long postId) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<CommunityFavorite>().eq(CommunityFavorite::getPostId, postId));
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>().eq(CommunityPost::getId, postId)
                .set(CommunityPost::getFavoriteCount, count == null ? 0L : count));
    }

    private void refreshCommentLikeCount(Long commentId) {
        Long count = commentLikeMapper.selectCount(new LambdaQueryWrapper<CommunityCommentLike>().eq(CommunityCommentLike::getCommentId, commentId));
        commentMapper.update(null, new LambdaUpdateWrapper<CommunityComment>().eq(CommunityComment::getId, commentId)
                .set(CommunityComment::getLikeCount, count == null ? 0L : count));
    }

    private String normalize(String value) {
        require(hasText(value), "互动事件字段不能为空");
        return value.trim().toUpperCase();
    }
}
