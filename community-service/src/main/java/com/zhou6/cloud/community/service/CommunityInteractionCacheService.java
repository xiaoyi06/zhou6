package com.zhou6.cloud.community.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.community.mq.CommunityInteractionMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

@Service
public class CommunityInteractionCacheService {

    private static final String POST = "POST";
    private static final String COMMENT = "COMMENT";
    private static final String LIKE = "LIKE";
    private static final String FAVORITE = "FAVORITE";
    private static final String VIEW = "VIEW";
    private static final String PENDING_KEY = "zhou6:community:interaction:pending";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> interactionScript;

    public CommunityInteractionCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.interactionScript = loadScript("lua/community_interaction.lua");
    }

    public boolean postLike(Long postId, Long userId, CommunityInteractionMessage message) {
        return execute(relationKey(POST, LIKE, postId), countKey(POST, LIKE, postId), LIKE, userId, message) == 1;
    }

    public boolean postUnlike(Long postId, Long userId, CommunityInteractionMessage message) {
        return execute(relationKey(POST, LIKE, postId), countKey(POST, LIKE, postId), "UNLIKE", userId, message) == 1;
    }

    public boolean postFavorite(Long postId, Long userId, CommunityInteractionMessage message) {
        return execute(relationKey(POST, FAVORITE, postId), countKey(POST, FAVORITE, postId), FAVORITE, userId, message) == 1;
    }

    public boolean postUnfavorite(Long postId, Long userId, CommunityInteractionMessage message) {
        return execute(relationKey(POST, FAVORITE, postId), countKey(POST, FAVORITE, postId), "UNFAVORITE", userId, message) == 1;
    }

    public boolean commentLike(Long commentId, Long userId, CommunityInteractionMessage message) {
        return execute(relationKey(COMMENT, LIKE, commentId), countKey(COMMENT, LIKE, commentId), LIKE, userId, message) == 1;
    }

    public boolean commentUnlike(Long commentId, Long userId, CommunityInteractionMessage message) {
        return execute(relationKey(COMMENT, LIKE, commentId), countKey(COMMENT, LIKE, commentId), "UNLIKE", userId, message) == 1;
    }

    public void postView(Long postId, CommunityInteractionMessage message) {
        execute("", countKey(POST, VIEW, postId), VIEW, 0L, message);
    }

    public boolean isPostLiked(Long postId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(relationKey(POST, LIKE, postId), String.valueOf(userId)));
    }

    public boolean hasPostLikeCache(Long postId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(relationKey(POST, LIKE, postId)));
    }

    public boolean isPostFavorited(Long postId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(relationKey(POST, FAVORITE, postId), String.valueOf(userId)));
    }

    public boolean hasPostFavoriteCache(Long postId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(relationKey(POST, FAVORITE, postId)));
    }

    public boolean isCommentLiked(Long commentId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(relationKey(COMMENT, LIKE, commentId), String.valueOf(userId)));
    }

    public boolean hasCommentLikeCache(Long commentId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(relationKey(COMMENT, LIKE, commentId)));
    }

    public long count(String targetType, String counterType, Long targetId, long dbValue) {
        String value = redisTemplate.opsForValue().get(countKey(targetType, counterType, targetId));
        if (value == null) {
            return dbValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return dbValue;
        }
    }

    public void initCountIfAbsent(String targetType, String counterType, Long targetId, long dbValue) {
        redisTemplate.opsForValue().setIfAbsent(countKey(targetType, counterType, targetId), String.valueOf(dbValue));
    }

    public void cachePostLiked(Long postId, Long userId) {
        redisTemplate.opsForSet().add(relationKey(POST, LIKE, postId), String.valueOf(userId));
    }

    public void cachePostFavorited(Long postId, Long userId) {
        redisTemplate.opsForSet().add(relationKey(POST, FAVORITE, postId), String.valueOf(userId));
    }

    public void cacheCommentLiked(Long commentId, Long userId) {
        redisTemplate.opsForSet().add(relationKey(COMMENT, LIKE, commentId), String.valueOf(userId));
    }

    public Set<String> pendingEvents(int batchSize) {
        return redisTemplate.opsForZSet().range(PENDING_KEY, 0, Math.max(0, batchSize - 1));
    }

    public void removePending(String eventJson) {
        redisTemplate.opsForZSet().remove(PENDING_KEY, eventJson);
    }

    public String pendingKey() {
        return PENDING_KEY;
    }

    private long execute(String relationKey, String countKey, String action, Long userId, CommunityInteractionMessage message) {
        try {
            String eventJson = objectMapper.writeValueAsString(message);
            Long result = redisTemplate.execute(interactionScript,
                    List.of(safeKey(relationKey), countKey, PENDING_KEY),
                    action,
                    String.valueOf(userId),
                    eventJson,
                    String.valueOf(Instant.now().toEpochMilli()));
            return result == null ? 0L : result;
        } catch (JsonProcessingException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "社区互动事件序列化失败", ex);
        }
    }

    private String safeKey(String key) {
        return key == null || key.isBlank() ? "zhou6:community:interaction:none" : key;
    }

    private String relationKey(String targetType, String relationType, Long targetId) {
        return "zhou6:community:" + targetType.toLowerCase() + ":" + relationType.toLowerCase() + ":" + targetId;
    }

    private String countKey(String targetType, String counterType, Long targetId) {
        return "zhou6:community:" + targetType.toLowerCase() + ":" + counterType.toLowerCase() + ":count:" + targetId;
    }

    private DefaultRedisScript<Long> loadScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(path)));
        return script;
    }
}
