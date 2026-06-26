package com.zhou6.cloud.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.community.dto.TagReserveDTO;
import com.zhou6.cloud.community.entity.CommunityPostTag;
import com.zhou6.cloud.community.mapper.CommunityPostTagMapper;
import com.zhou6.cloud.community.service.CommunityTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityTagServiceImpl extends BaseCommunityService implements CommunityTagService {

    private final CommunityPostTagMapper postTagMapper;

    public CommunityTagServiceImpl(CommunityPostTagMapper postTagMapper) {
        this.postTagMapper = postTagMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reservePostTags(TagReserveDTO dto) {
        Long postId = parseRequiredId(dto == null ? null : dto.getPostId(), "帖子ID不能为空");
        postTagMapper.delete(new LambdaQueryWrapper<CommunityPostTag>().eq(CommunityPostTag::getPostId, postId));
        if (dto.getTagIds() == null) {
            return;
        }
        for (String tagId : dto.getTagIds().stream().filter(this::hasText).distinct().toList()) {
            CommunityPostTag relation = new CommunityPostTag();
            relation.setPostId(postId);
            relation.setTagId(parseRequiredId(tagId, "标签ID不正确"));
            postTagMapper.insert(relation);
        }
    }
}
