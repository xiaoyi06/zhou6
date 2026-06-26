package com.zhou6.cloud.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.community.dto.PageQueryDTO;
import com.zhou6.cloud.community.dto.UserIdDTO;
import com.zhou6.cloud.community.entity.CommunityUserBlock;
import com.zhou6.cloud.community.entity.CommunityUserFavorite;
import com.zhou6.cloud.community.entity.CommunityUserFollow;
import com.zhou6.cloud.community.mapper.CommunityUserBlockMapper;
import com.zhou6.cloud.community.mapper.CommunityUserFavoriteMapper;
import com.zhou6.cloud.community.mapper.CommunityUserFollowMapper;
import com.zhou6.cloud.community.service.CommunityRelationService;
import com.zhou6.cloud.community.vo.PageResponse;
import com.zhou6.cloud.community.vo.RelationUserVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class CommunityRelationServiceImpl extends BaseCommunityService implements CommunityRelationService {

    private final CommunityUserFollowMapper followMapper;
    private final CommunityUserFavoriteMapper favoriteMapper;
    private final CommunityUserBlockMapper blockMapper;

    public CommunityRelationServiceImpl(CommunityUserFollowMapper followMapper,
            CommunityUserFavoriteMapper favoriteMapper,
            CommunityUserBlockMapper blockMapper) {
        this.followMapper = followMapper;
        this.favoriteMapper = favoriteMapper;
        this.blockMapper = blockMapper;
    }

    @Override
    public void follow(UserIdDTO dto) {
        Long userId = currentUserId();
        Long targetUserId = targetUserId(dto);
        if (followMapper.selectCount(new LambdaQueryWrapper<CommunityUserFollow>()
                .eq(CommunityUserFollow::getUserId, userId)
                .eq(CommunityUserFollow::getTargetUserId, targetUserId)) > 0) {
            return;
        }
        CommunityUserFollow relation = new CommunityUserFollow();
        relation.setUserId(userId);
        relation.setTargetUserId(targetUserId);
        try {
            followMapper.insert(relation);
        } catch (DuplicateKeyException ex) {
            return;
        }
    }

    @Override
    public void unfollow(UserIdDTO dto) {
        Long userId = currentUserId();
        followMapper.delete(new LambdaQueryWrapper<CommunityUserFollow>()
                .eq(CommunityUserFollow::getUserId, userId)
                .eq(CommunityUserFollow::getTargetUserId, targetUserId(dto)));
    }

    @Override
    public void favoriteUser(UserIdDTO dto) {
        Long userId = currentUserId();
        Long targetUserId = targetUserId(dto);
        if (favoriteMapper.selectCount(new LambdaQueryWrapper<CommunityUserFavorite>()
                .eq(CommunityUserFavorite::getUserId, userId)
                .eq(CommunityUserFavorite::getTargetUserId, targetUserId)) > 0) {
            return;
        }
        CommunityUserFavorite relation = new CommunityUserFavorite();
        relation.setUserId(userId);
        relation.setTargetUserId(targetUserId);
        try {
            favoriteMapper.insert(relation);
        } catch (DuplicateKeyException ex) {
            return;
        }
    }

    @Override
    public void unfavoriteUser(UserIdDTO dto) {
        Long userId = currentUserId();
        favoriteMapper.delete(new LambdaQueryWrapper<CommunityUserFavorite>()
                .eq(CommunityUserFavorite::getUserId, userId)
                .eq(CommunityUserFavorite::getTargetUserId, targetUserId(dto)));
    }

    @Override
    public void block(UserIdDTO dto) {
        Long userId = currentUserId();
        Long targetUserId = targetUserId(dto);
        if (blockMapper.selectCount(new LambdaQueryWrapper<CommunityUserBlock>()
                .eq(CommunityUserBlock::getUserId, userId)
                .eq(CommunityUserBlock::getTargetUserId, targetUserId)) > 0) {
            return;
        }
        CommunityUserBlock relation = new CommunityUserBlock();
        relation.setUserId(userId);
        relation.setTargetUserId(targetUserId);
        try {
            blockMapper.insert(relation);
        } catch (DuplicateKeyException ex) {
            return;
        }
    }

    @Override
    public void unblock(UserIdDTO dto) {
        Long userId = currentUserId();
        blockMapper.delete(new LambdaQueryWrapper<CommunityUserBlock>()
                .eq(CommunityUserBlock::getUserId, userId)
                .eq(CommunityUserBlock::getTargetUserId, targetUserId(dto)));
    }

    @Override
    public PageResponse<RelationUserVO> followPage(PageQueryDTO dto) {
        PageQueryDTO query = dto == null ? new PageQueryDTO() : dto;
        Page<CommunityUserFollow> page = followMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<CommunityUserFollow>().eq(CommunityUserFollow::getUserId, currentUserId()).orderByDesc(CommunityUserFollow::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(item -> followVo(String.valueOf(item.getTargetUserId()), "FOLLOW", item.getCreateTime())).toList());
    }

    @Override
    public PageResponse<RelationUserVO> favoriteUserPage(PageQueryDTO dto) {
        PageQueryDTO query = dto == null ? new PageQueryDTO() : dto;
        Page<CommunityUserFavorite> page = favoriteMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<CommunityUserFavorite>().eq(CommunityUserFavorite::getUserId, currentUserId()).orderByDesc(CommunityUserFavorite::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(item -> followVo(String.valueOf(item.getTargetUserId()), "FAVORITE_USER", item.getCreateTime())).toList());
    }

    @Override
    public PageResponse<RelationUserVO> blockPage(PageQueryDTO dto) {
        PageQueryDTO query = dto == null ? new PageQueryDTO() : dto;
        Page<CommunityUserBlock> page = blockMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<CommunityUserBlock>().eq(CommunityUserBlock::getUserId, currentUserId()).orderByDesc(CommunityUserBlock::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(item -> followVo(String.valueOf(item.getTargetUserId()), "BLOCK", item.getCreateTime())).toList());
    }

    private RelationUserVO followVo(String userId, String type, java.time.LocalDateTime createTime) {
        RelationUserVO vo = new RelationUserVO();
        vo.setUserId(userId);
        vo.setRelationType(type);
        vo.setCreateTime(createTime);
        return vo;
    }

    private Long targetUserId(UserIdDTO dto) {
        Long current = currentUserId();
        Long target = parseRequiredId(dto == null ? null : dto.getUserId(), "用户ID不能为空");
        require(!current.equals(target), "不能操作自己");
        return target;
    }

    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        require(userId != null && userId > 0, "当前用户不存在");
        return userId;
    }
}
