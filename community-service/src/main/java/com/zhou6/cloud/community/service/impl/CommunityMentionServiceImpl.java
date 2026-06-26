package com.zhou6.cloud.community.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.community.client.UserClient;
import com.zhou6.cloud.community.dto.MentionCandidateQueryDTO;
import com.zhou6.cloud.community.dto.UserSearchDTO;
import com.zhou6.cloud.community.entity.CommunityUserBlock;
import com.zhou6.cloud.community.entity.CommunityUserFavorite;
import com.zhou6.cloud.community.entity.CommunityUserFollow;
import com.zhou6.cloud.community.mapper.CommunityUserBlockMapper;
import com.zhou6.cloud.community.mapper.CommunityUserFavoriteMapper;
import com.zhou6.cloud.community.mapper.CommunityUserFollowMapper;
import com.zhou6.cloud.community.service.CommunityMentionService;
import com.zhou6.cloud.community.vo.MentionCandidateVO;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.UserSearchVO;
import org.springframework.stereotype.Service;

@Service
public class CommunityMentionServiceImpl extends BaseCommunityService implements CommunityMentionService {

    private final UserClient userClient;
    private final CommunityUserFollowMapper followMapper;
    private final CommunityUserFavoriteMapper favoriteMapper;
    private final CommunityUserBlockMapper blockMapper;

    public CommunityMentionServiceImpl(UserClient userClient,
            CommunityUserFollowMapper followMapper,
            CommunityUserFavoriteMapper favoriteMapper,
            CommunityUserBlockMapper blockMapper) {
        this.userClient = userClient;
        this.followMapper = followMapper;
        this.favoriteMapper = favoriteMapper;
        this.blockMapper = blockMapper;
    }

    @Override
    public PageResponse<MentionCandidateVO> candidates(MentionCandidateQueryDTO dto) {
        Long userId = currentUserId();
        MentionCandidateQueryDTO query = dto == null ? new MentionCandidateQueryDTO() : dto;
        Set<Long> followed = followMapper.selectList(new LambdaQueryWrapper<CommunityUserFollow>().eq(CommunityUserFollow::getUserId, userId))
                .stream().map(CommunityUserFollow::getTargetUserId).collect(Collectors.toSet());
        Set<Long> favoriteUsers = favoriteMapper.selectList(new LambdaQueryWrapper<CommunityUserFavorite>().eq(CommunityUserFavorite::getUserId, userId))
                .stream().map(CommunityUserFavorite::getTargetUserId).collect(Collectors.toSet());
        Set<Long> blocked = blockMapper.selectList(new LambdaQueryWrapper<CommunityUserBlock>().eq(CommunityUserBlock::getUserId, userId))
                .stream().map(CommunityUserBlock::getTargetUserId).collect(Collectors.toSet());
        Map<Long, MentionCandidateVO> result = new LinkedHashMap<>();
        if (!hasText(query.getKeyword())) {
            for (Long candidateId : favoriteUsers) {
                addRelationCandidate(result, candidateId, followed, favoriteUsers, blocked);
            }
            for (Long candidateId : followed) {
                addRelationCandidate(result, candidateId, followed, favoriteUsers, blocked);
            }
        }
        for (UserSearchVO user : searchUsers(query)) {
            Long candidateId = parseNullableId(user.getId(), "用户ID不正确");
            if (candidateId == null || candidateId.equals(userId) || blocked.contains(candidateId)) {
                continue;
            }
            MentionCandidateVO vo = result.computeIfAbsent(candidateId, id -> new MentionCandidateVO());
            vo.setUserId(String.valueOf(candidateId));
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setFollowed(followed.contains(candidateId));
            vo.setFavoriteUser(favoriteUsers.contains(candidateId));
            vo.setPriority(priority(candidateId, followed, favoriteUsers));
        }
        List<MentionCandidateVO> records = new ArrayList<>(result.values()).stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .limit(pageSize(query.getPageSize()))
                .toList();
        return new PageResponse<>(records.size(), 1, records.size(), records);
    }

    private List<UserSearchVO> searchUsers(MentionCandidateQueryDTO query) {
        UserSearchDTO dto = new UserSearchDTO();
        dto.setPageNum(1);
        dto.setPageSize((int) pageSize(query.getPageSize()));
        if (hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            if (containsCjk(keyword)) {
                dto.setNickname(keyword);
            } else {
                dto.setUsername(keyword);
            }
        }
        R<PageResponse<UserSearchVO>> response = userClient.page(dto);
        if (response == null || response.getData() == null || response.getData().getRecords() == null) {
            return List.of();
        }
        return response.getData().getRecords();
    }

    private void addRelationCandidate(Map<Long, MentionCandidateVO> result, Long candidateId,
            Set<Long> followed, Set<Long> favoriteUsers, Set<Long> blocked) {
        Long userId = currentUserId();
        if (candidateId == null || candidateId.equals(userId) || blocked.contains(candidateId)) {
            return;
        }
        MentionCandidateVO vo = new MentionCandidateVO();
        vo.setUserId(String.valueOf(candidateId));
        vo.setFollowed(followed.contains(candidateId));
        vo.setFavoriteUser(favoriteUsers.contains(candidateId));
        vo.setPriority(priority(candidateId, followed, favoriteUsers));
        result.put(candidateId, vo);
    }

    private int priority(Long candidateId, Set<Long> followed, Set<Long> favoriteUsers) {
        int priority = 0;
        if (favoriteUsers.contains(candidateId)) {
            priority += 20;
        }
        if (followed.contains(candidateId)) {
            priority += 10;
        }
        return priority;
    }

    private boolean containsCjk(String value) {
        for (int i = 0; i < value.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(value.charAt(i));
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
                    || Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
                    || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)) {
                return true;
            }
        }
        return false;
    }

    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        require(userId != null && userId > 0, "当前用户不存在");
        return userId;
    }
}
