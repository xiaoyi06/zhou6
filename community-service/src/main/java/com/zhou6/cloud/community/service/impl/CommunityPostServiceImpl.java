package com.zhou6.cloud.community.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.community.dto.IdDTO;
import com.zhou6.cloud.community.dto.PostAuditDTO;
import com.zhou6.cloud.community.dto.PostPageQueryDTO;
import com.zhou6.cloud.community.dto.PostResourceDTO;
import com.zhou6.cloud.community.dto.PostSaveDTO;
import com.zhou6.cloud.community.entity.CommunityFavorite;
import com.zhou6.cloud.community.entity.CommunityMention;
import com.zhou6.cloud.community.entity.CommunityPost;
import com.zhou6.cloud.community.entity.CommunityPostLike;
import com.zhou6.cloud.community.entity.CommunityPostResource;
import com.zhou6.cloud.community.entity.CommunityPostTag;
import com.zhou6.cloud.community.entity.CommunityTag;
import com.zhou6.cloud.community.entity.CommunityUserBlock;
import com.zhou6.cloud.community.entity.CommunityUserFavorite;
import com.zhou6.cloud.community.entity.CommunityUserFollow;
import com.zhou6.cloud.community.mapper.CommunityFavoriteMapper;
import com.zhou6.cloud.community.mapper.CommunityMentionMapper;
import com.zhou6.cloud.community.mapper.CommunityPostLikeMapper;
import com.zhou6.cloud.community.mapper.CommunityPostMapper;
import com.zhou6.cloud.community.mapper.CommunityPostResourceMapper;
import com.zhou6.cloud.community.mapper.CommunityPostTagMapper;
import com.zhou6.cloud.community.mapper.CommunityTagMapper;
import com.zhou6.cloud.community.mapper.CommunityUserBlockMapper;
import com.zhou6.cloud.community.mapper.CommunityUserFavoriteMapper;
import com.zhou6.cloud.community.mapper.CommunityUserFollowMapper;
import com.zhou6.cloud.community.mq.CommunityInteractionMessage;
import com.zhou6.cloud.community.mq.CommunityInteractionPublisher;
import com.zhou6.cloud.community.service.CommunityPostService;
import com.zhou6.cloud.community.service.CommunityInteractionCacheService;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.PostResourceVO;
import com.zhou6.cloud.community.vo.PostVO;
import com.zhou6.cloud.community.vo.TagVO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityPostServiceImpl extends BaseCommunityService implements CommunityPostService {

    private static final String AUDIT_APPROVED = "APPROVED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String BUSINESS_POST = "POST";

    private final CommunityPostMapper postMapper;
    private final CommunityPostResourceMapper resourceMapper;
    private final CommunityPostLikeMapper postLikeMapper;
    private final CommunityFavoriteMapper favoriteMapper;
    private final CommunityMentionMapper mentionMapper;
    private final CommunityPostTagMapper postTagMapper;
    private final CommunityTagMapper tagMapper;
    private final CommunityUserFollowMapper followMapper;
    private final CommunityUserFavoriteMapper userFavoriteMapper;
    private final CommunityUserBlockMapper blockMapper;
    private final CommunityInteractionCacheService interactionCacheService;
    private final CommunityInteractionPublisher interactionPublisher;

    public CommunityPostServiceImpl(CommunityPostMapper postMapper,
            CommunityPostResourceMapper resourceMapper,
            CommunityPostLikeMapper postLikeMapper,
            CommunityFavoriteMapper favoriteMapper,
            CommunityMentionMapper mentionMapper,
            CommunityPostTagMapper postTagMapper,
            CommunityTagMapper tagMapper,
            CommunityUserFollowMapper followMapper,
            CommunityUserFavoriteMapper userFavoriteMapper,
            CommunityUserBlockMapper blockMapper,
            CommunityInteractionCacheService interactionCacheService,
            CommunityInteractionPublisher interactionPublisher) {
        this.postMapper = postMapper;
        this.resourceMapper = resourceMapper;
        this.postLikeMapper = postLikeMapper;
        this.favoriteMapper = favoriteMapper;
        this.mentionMapper = mentionMapper;
        this.postTagMapper = postTagMapper;
        this.tagMapper = tagMapper;
        this.followMapper = followMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.blockMapper = blockMapper;
        this.interactionCacheService = interactionCacheService;
        this.interactionPublisher = interactionPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(PostSaveDTO dto) {
        Long userId = currentUserId();
        validatePost(dto);
        CommunityPost post = new CommunityPost();
        post.setAuthorUserId(userId);
        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent().trim());
        post.setAuditStatus(AUDIT_APPROVED);
        post.setPublishStatus(STATUS_PUBLISHED);
        post.setTopFlag(0);
        post.setFeaturedFlag(0);
        post.setViewCount(0L);
        post.setLikeCount(0L);
        post.setCommentCount(0L);
        post.setFavoriteCount(0L);
        post.setHeatScore(BigDecimal.ZERO);
        postMapper.insert(post);
        saveResources(post.getId(), dto.getResources());
        savePostTags(post.getId(), dto.getTagIds());
        saveMentions(BUSINESS_POST, post.getId(), post.getId(), userId, dto.getMentionUserIds());
        updateHeat(post.getId());
        return String.valueOf(post.getId());
    }

    @Override
    public PageResponse<PostVO> page(PostPageQueryDTO dto) {
        Long userId = UserContextHolder.getUserId();
        PostPageQueryDTO query = dto == null ? new PostPageQueryDTO() : dto;
        LambdaQueryWrapper<CommunityPost> wrapper = basePageQuery(userId, query)
                .orderByDesc(CommunityPost::getTopFlag)
                .orderByDesc(CommunityPost::getFeaturedFlag)
                .orderByDesc(CommunityPost::getCreateTime)
                .orderByDesc(CommunityPost::getId);
        Page<CommunityPost> page = postMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())), wrapper);
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), toVos(page.getRecords(), userId));
    }

    @Override
    public PageResponse<PostVO> hot(PostPageQueryDTO dto) {
        Long userId = UserContextHolder.getUserId();
        PostPageQueryDTO query = dto == null ? new PostPageQueryDTO() : dto;
        Page<CommunityPost> page = postMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                basePageQuery(userId, query)
                        .orderByDesc(CommunityPost::getHeatScore)
                        .orderByDesc(CommunityPost::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), toVos(page.getRecords(), userId));
    }

    @Override
    public PostVO detail(IdDTO dto) {
        Long userId = UserContextHolder.getUserId();
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        CommunityPost post = requirePost(id, true);
        if (userId != null && !userId.equals(post.getAuthorUserId())) {
            interactionCacheService.initCountIfAbsent("POST", "VIEW", id, valueOrZero(post.getViewCount()));
            CommunityInteractionMessage message = message("VIEW", "POST", id, userId);
            interactionCacheService.postView(id, message);
            interactionPublisher.publish(message);
        }
        return toVo(post, userId, resourceMap(List.of(id)), tagMap(List.of(id)), likedIds(List.of(id), userId), favoritedIds(List.of(id), userId));
    }

    @Override
    public void delete(IdDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        CommunityPost post = requirePost(id, false);
        Long userId = currentUserId();
        require(userId.equals(post.getAuthorUserId()), "只能删除自己的帖子");
        softDelete(id);
    }

    @Override
    public void restore(IdDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, id)
                .set(CommunityPost::getDeletedAt, null));
    }

    @Override
    public void audit(PostAuditDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        requirePost(id, false);
        String auditStatus = normalizeAuditStatus(dto.getAuditStatus());
        String publishStatus = normalizePublishStatus(dto.getPublishStatus());
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, id)
                .set(CommunityPost::getAuditStatus, auditStatus)
                .set(CommunityPost::getPublishStatus, publishStatus));
    }

    @Override
    public void top(IdDTO dto) {
        updateFlag(dto, true, true);
    }

    @Override
    public void untop(IdDTO dto) {
        updateFlag(dto, true, false);
    }

    @Override
    public void feature(IdDTO dto) {
        updateFlag(dto, false, true);
    }

    @Override
    public void unfeature(IdDTO dto) {
        updateFlag(dto, false, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(IdDTO dto) {
        Long userId = currentUserId();
        Long postId = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        CommunityPost post = requirePost(postId, true);
        warmPostLike(postId, userId, post);
        CommunityInteractionMessage message = message("LIKE", "POST", postId, userId);
        if (interactionCacheService.postLike(postId, userId, message)) {
            interactionPublisher.publish(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(IdDTO dto) {
        Long userId = currentUserId();
        Long postId = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        CommunityPost post = requirePost(postId, true);
        warmPostLike(postId, userId, post);
        CommunityInteractionMessage message = message("UNLIKE", "POST", postId, userId);
        if (interactionCacheService.postUnlike(postId, userId, message)) {
            interactionPublisher.publish(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(IdDTO dto) {
        Long userId = currentUserId();
        Long postId = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        CommunityPost post = requirePost(postId, true);
        warmPostFavorite(postId, userId, post);
        CommunityInteractionMessage message = message("FAVORITE", "POST", postId, userId);
        if (interactionCacheService.postFavorite(postId, userId, message)) {
            interactionPublisher.publish(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(IdDTO dto) {
        Long userId = currentUserId();
        Long postId = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        CommunityPost post = requirePost(postId, true);
        warmPostFavorite(postId, userId, post);
        CommunityInteractionMessage message = message("UNFAVORITE", "POST", postId, userId);
        if (interactionCacheService.postUnfavorite(postId, userId, message)) {
            interactionPublisher.publish(message);
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${zhou6.community.heat-refresh-delay-ms:300000}")
    public void refreshHeat() {
        List<CommunityPost> posts = postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                .isNull(CommunityPost::getDeletedAt)
                .eq(CommunityPost::getPublishStatus, STATUS_PUBLISHED)
                .last("limit 500"));
        for (CommunityPost post : posts) {
            postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, post.getId())
                    .set(CommunityPost::getHeatScore, calculateHeat(post)));
        }
    }

    public void increaseCommentCount(Long postId) {
        requirePost(postId, true);
        incrementPostCounter(postId, "comment_count");
        updateHeat(postId);
    }

    public void decreaseCommentCount(Long postId) {
        requirePost(postId, true);
        decrementPostCounter(postId, "comment_count");
        updateHeat(postId);
    }

    private LambdaQueryWrapper<CommunityPost> basePageQuery(Long userId, PostPageQueryDTO query) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .isNull(CommunityPost::getDeletedAt)
                .eq(CommunityPost::getPublishStatus, STATUS_PUBLISHED)
                .eq(CommunityPost::getAuditStatus, AUDIT_APPROVED)
                .like(hasText(query.getKeyword()), CommunityPost::getTitle, query.getKeyword())
                .ge(query.getBeginTime() != null, CommunityPost::getCreateTime, query.getBeginTime())
                .le(query.getEndTime() != null, CommunityPost::getCreateTime, query.getEndTime());
        if (userId != null) {
            List<Long> blocked = blockedUserIds(userId);
            if (!blocked.isEmpty()) {
                wrapper.notIn(CommunityPost::getAuthorUserId, blocked);
            }
            applyScope(wrapper, userId, query.getScope());
        }
        if (query.getTagIds() != null && !query.getTagIds().isEmpty()) {
            List<Long> tagIds = query.getTagIds().stream().map(id -> parseRequiredId(id, "标签ID不正确")).distinct().toList();
            List<Long> postIds = postTagMapper.selectList(new LambdaQueryWrapper<CommunityPostTag>()
                            .in(CommunityPostTag::getTagId, tagIds))
                    .stream().map(CommunityPostTag::getPostId).distinct().toList();
            wrapper.in(!postIds.isEmpty(), CommunityPost::getId, postIds);
            if (postIds.isEmpty()) {
                wrapper.eq(CommunityPost::getId, -1L);
            }
        }
        return wrapper;
    }

    private void applyScope(LambdaQueryWrapper<CommunityPost> wrapper, Long userId, String scope) {
        String normalized = hasText(scope) ? scope.trim().toUpperCase() : "ALL";
        if ("MINE".equals(normalized)) {
            wrapper.eq(CommunityPost::getAuthorUserId, userId);
            return;
        }
        if ("FOLLOWING".equals(normalized)) {
            wrapper.in(CommunityPost::getAuthorUserId, relationUserIds(followMapper.selectList(
                    new LambdaQueryWrapper<CommunityUserFollow>().eq(CommunityUserFollow::getUserId, userId)), CommunityUserFollow::getTargetUserId));
            return;
        }
        if ("FAVORITE_USER".equals(normalized)) {
            wrapper.in(CommunityPost::getAuthorUserId, relationUserIds(userFavoriteMapper.selectList(
                    new LambdaQueryWrapper<CommunityUserFavorite>().eq(CommunityUserFavorite::getUserId, userId)), CommunityUserFavorite::getTargetUserId));
            return;
        }
        if ("FAVORITED_POST".equals(normalized)) {
            List<Long> postIds = favoriteMapper.selectList(new LambdaQueryWrapper<CommunityFavorite>()
                            .eq(CommunityFavorite::getUserId, userId))
                    .stream().map(CommunityFavorite::getPostId).toList();
            wrapper.in(!postIds.isEmpty(), CommunityPost::getId, postIds);
            if (postIds.isEmpty()) {
                wrapper.eq(CommunityPost::getId, -1L);
            }
        }
    }

    private <T> List<Long> relationUserIds(List<T> relations, Function<T, Long> mapper) {
        List<Long> ids = relations.stream().map(mapper).distinct().toList();
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    private List<PostVO> toVos(List<CommunityPost> posts, Long userId) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = posts.stream().map(CommunityPost::getId).toList();
        Map<Long, List<PostResourceVO>> resources = resourceMap(ids);
        Map<Long, List<TagVO>> tags = tagMap(ids);
        Set<Long> liked = likedIds(ids, userId);
        Set<Long> favorited = favoritedIds(ids, userId);
        return posts.stream().map(post -> toVo(post, userId, resources, tags, liked, favorited)).toList();
    }

    private PostVO toVo(CommunityPost post, Long userId, Map<Long, List<PostResourceVO>> resources,
            Map<Long, List<TagVO>> tags, Set<Long> liked, Set<Long> favorited) {
        PostVO vo = new PostVO();
        vo.setId(String.valueOf(post.getId()));
        vo.setAuthorUserId(String.valueOf(post.getAuthorUserId()));
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setAuditStatus(post.getAuditStatus());
        vo.setPublishStatus(post.getPublishStatus());
        vo.setTop(Integer.valueOf(1).equals(post.getTopFlag()));
        vo.setFeatured(Integer.valueOf(1).equals(post.getFeaturedFlag()));
        vo.setViewCount(String.valueOf(interactionCacheService.count("POST", "VIEW", post.getId(), valueOrZero(post.getViewCount()))));
        vo.setLikeCount(String.valueOf(interactionCacheService.count("POST", "LIKE", post.getId(), valueOrZero(post.getLikeCount()))));
        vo.setCommentCount(String.valueOf(valueOrZero(post.getCommentCount())));
        vo.setFavoriteCount(String.valueOf(interactionCacheService.count("POST", "FAVORITE", post.getId(), valueOrZero(post.getFavoriteCount()))));
        vo.setHeatScore(post.getHeatScore());
        vo.setLiked(userId != null && liked.contains(post.getId()));
        vo.setFavorited(userId != null && favorited.contains(post.getId()));
        vo.setResources(resources.getOrDefault(post.getId(), Collections.emptyList()));
        vo.setTags(tags.getOrDefault(post.getId(), Collections.emptyList()));
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());
        return vo;
    }

    private Map<Long, List<PostResourceVO>> resourceMap(List<Long> postIds) {
        return resourceMapper.selectList(new LambdaQueryWrapper<CommunityPostResource>().in(CommunityPostResource::getPostId, postIds))
                .stream().map(resource -> {
                    PostResourceVO vo = new PostResourceVO();
                    vo.setId(String.valueOf(resource.getId()));
                    vo.setPostId(String.valueOf(resource.getPostId()));
                    vo.setFileId(String.valueOf(resource.getFileId()));
                    vo.setResourceType(resource.getResourceType());
                    vo.setSortOrder(resource.getSortOrder());
                    return vo;
                }).collect(Collectors.groupingBy(vo -> parseRequiredId(vo.getPostId(), "帖子ID不正确")));
    }

    private Map<Long, List<TagVO>> tagMap(List<Long> postIds) {
        List<CommunityPostTag> relations = postTagMapper.selectList(new LambdaQueryWrapper<CommunityPostTag>().in(CommunityPostTag::getPostId, postIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, CommunityTag> tags = tagMapper.selectList(new LambdaQueryWrapper<CommunityTag>()
                        .in(CommunityTag::getId, relations.stream().map(CommunityPostTag::getTagId).distinct().toList()))
                .stream().collect(Collectors.toMap(CommunityTag::getId, Function.identity()));
        Map<Long, List<TagVO>> result = new java.util.HashMap<>();
        for (CommunityPostTag relation : relations) {
            CommunityTag tag = tags.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            TagVO vo = new TagVO();
            vo.setId(String.valueOf(tag.getId()));
            vo.setTagName(tag.getTagName());
            result.computeIfAbsent(relation.getPostId(), key -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    private Set<Long> likedIds(List<Long> postIds, Long userId) {
        if (userId == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return postLikeMapper.selectList(new LambdaQueryWrapper<CommunityPostLike>()
                        .eq(CommunityPostLike::getUserId, userId)
                        .in(CommunityPostLike::getPostId, postIds))
                .stream().map(CommunityPostLike::getPostId).collect(Collectors.toSet());
    }

    private Set<Long> favoritedIds(List<Long> postIds, Long userId) {
        if (userId == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return favoriteMapper.selectList(new LambdaQueryWrapper<CommunityFavorite>()
                        .eq(CommunityFavorite::getUserId, userId)
                        .in(CommunityFavorite::getPostId, postIds))
                .stream().map(CommunityFavorite::getPostId).collect(Collectors.toSet());
    }

    private void saveResources(Long postId, List<PostResourceDTO> resources) {
        if (resources == null || resources.isEmpty()) {
            return;
        }
        for (int i = 0; i < resources.size(); i++) {
            PostResourceDTO dto = resources.get(i);
            Long fileId = parseRequiredId(dto.getFileId(), "资源文件ID不能为空");
            CommunityPostResource resource = new CommunityPostResource();
            resource.setPostId(postId);
            resource.setFileId(fileId);
            resource.setResourceType(hasText(dto.getResourceType()) ? dto.getResourceType().trim().toUpperCase() : "IMAGE");
            resource.setSortOrder(dto.getSortOrder() == null ? i : dto.getSortOrder());
            resourceMapper.insert(resource);
        }
    }

    private void savePostTags(Long postId, List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (String tagId : tagIds.stream().filter(this::hasText).distinct().toList()) {
            CommunityPostTag relation = new CommunityPostTag();
            relation.setPostId(postId);
            relation.setTagId(parseRequiredId(tagId, "标签ID不正确"));
            postTagMapper.insert(relation);
        }
    }

    private void saveMentions(String businessType, Long businessId, Long postId, Long operatorUserId, List<String> mentionUserIds) {
        if (mentionUserIds == null || mentionUserIds.isEmpty()) {
            return;
        }
        List<Long> blocked = blockedUserIds(operatorUserId);
        for (Long mentionedUserId : mentionUserIds.stream().filter(this::hasText)
                .map(id -> parseRequiredId(id, "@用户ID不正确"))
                .filter(id -> !id.equals(operatorUserId))
                .filter(id -> !blocked.contains(id))
                .distinct().toList()) {
            CommunityMention mention = new CommunityMention();
            mention.setBusinessType(businessType);
            mention.setBusinessId(businessId);
            mention.setPostId(postId);
            mention.setOperatorUserId(operatorUserId);
            mention.setMentionedUserId(mentionedUserId);
            mentionMapper.insert(mention);
        }
    }

    private List<Long> blockedUserIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return blockMapper.selectList(new LambdaQueryWrapper<CommunityUserBlock>()
                        .eq(CommunityUserBlock::getUserId, userId))
                .stream().map(CommunityUserBlock::getTargetUserId).distinct().toList();
    }

    private void updateFlag(IdDTO dto, boolean top, boolean enabled) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "帖子ID不能为空");
        requirePost(id, false);
        LambdaUpdateWrapper<CommunityPost> wrapper = new LambdaUpdateWrapper<CommunityPost>().eq(CommunityPost::getId, id);
        if (top) {
            wrapper.set(CommunityPost::getTopFlag, enabled ? 1 : 0);
        } else {
            wrapper.set(CommunityPost::getFeaturedFlag, enabled ? 1 : 0);
        }
        postMapper.update(null, wrapper);
        updateHeat(id);
    }

    private void softDelete(Long id) {
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, id)
                .set(CommunityPost::getDeletedAt, LocalDateTime.now()));
    }

    private void incrementPostCounter(Long postId, String column) {
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, postId)
                .setSql(column + " = " + column + " + 1"));
    }

    private void warmPostLike(Long postId, Long userId, CommunityPost post) {
        interactionCacheService.initCountIfAbsent("POST", "LIKE", postId, valueOrZero(post.getLikeCount()));
        if (postLikeMapper.selectCount(new LambdaQueryWrapper<CommunityPostLike>()
                .eq(CommunityPostLike::getPostId, postId)
                .eq(CommunityPostLike::getUserId, userId)) > 0) {
            interactionCacheService.cachePostLiked(postId, userId);
        }
    }

    private void warmPostFavorite(Long postId, Long userId, CommunityPost post) {
        interactionCacheService.initCountIfAbsent("POST", "FAVORITE", postId, valueOrZero(post.getFavoriteCount()));
        if (favoriteMapper.selectCount(new LambdaQueryWrapper<CommunityFavorite>()
                .eq(CommunityFavorite::getPostId, postId)
                .eq(CommunityFavorite::getUserId, userId)) > 0) {
            interactionCacheService.cachePostFavorited(postId, userId);
        }
    }

    private CommunityInteractionMessage message(String action, String targetType, Long targetId, Long userId) {
        CommunityInteractionMessage message = new CommunityInteractionMessage();
        message.setEventId(java.util.UUID.randomUUID().toString());
        message.setAction(action);
        message.setTargetType(targetType);
        message.setTargetId(targetId);
        message.setUserId(userId);
        message.setEventTime(LocalDateTime.now());
        return message;
    }

    private void decrementPostCounter(Long postId, String column) {
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, postId)
                .setSql(column + " = GREATEST(" + column + " - 1, 0)"));
    }

    private void updateHeat(Long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post != null) {
            postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, postId)
                    .set(CommunityPost::getHeatScore, calculateHeat(post)));
        }
    }

    private BigDecimal calculateHeat(CommunityPost post) {
        long ageHours = post.getCreateTime() == null ? 0 : Math.max(0, Duration.between(post.getCreateTime(), LocalDateTime.now()).toHours());
        long viewCount = interactionCacheService.count("POST", "VIEW", post.getId(), valueOrZero(post.getViewCount()));
        long likeCount = interactionCacheService.count("POST", "LIKE", post.getId(), valueOrZero(post.getLikeCount()));
        long favoriteCount = interactionCacheService.count("POST", "FAVORITE", post.getId(), valueOrZero(post.getFavoriteCount()));
        long score = viewCount
                + likeCount * 3
                + valueOrZero(post.getCommentCount()) * 5
                + favoriteCount * 8
                + (Integer.valueOf(1).equals(post.getTopFlag()) ? 1000 : 0)
                + (Integer.valueOf(1).equals(post.getFeaturedFlag()) ? 500 : 0)
                - ageHours;
        return BigDecimal.valueOf(Math.max(0, score));
    }

    private CommunityPost requirePost(Long postId, boolean activeOnly) {
        CommunityPost post = postMapper.selectById(postId);
        require(post != null, "帖子不存在");
        if (activeOnly) {
            require(post.getDeletedAt() == null, "帖子已删除");
            require(STATUS_PUBLISHED.equals(post.getPublishStatus()), "帖子未发布");
            require(AUDIT_APPROVED.equals(post.getAuditStatus()), "帖子审核未通过");
        }
        return post;
    }

    private void validatePost(PostSaveDTO dto) {
        require(dto != null, "帖子参数不能为空");
        require(hasText(dto.getTitle()), "帖子标题不能为空");
        require(dto.getTitle().trim().length() <= 200, "帖子标题不能超过200个字符");
        require(hasText(dto.getContent()), "帖子内容不能为空");
        require(dto.getContent().trim().length() <= 10000, "帖子内容不能超过10000个字符");
    }

    private String normalizeAuditStatus(String value) {
        String status = hasText(value) ? value.trim().toUpperCase() : AUDIT_APPROVED;
        require("PENDING".equals(status) || AUDIT_APPROVED.equals(status) || "REJECTED".equals(status), "审核状态不正确");
        return status;
    }

    private String normalizePublishStatus(String value) {
        String status = hasText(value) ? value.trim().toUpperCase() : STATUS_PUBLISHED;
        require(STATUS_PUBLISHED.equals(status) || "HIDDEN".equals(status), "发布状态不正确");
        return status;
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
