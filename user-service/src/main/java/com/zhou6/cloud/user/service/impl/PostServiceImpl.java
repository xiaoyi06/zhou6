package com.zhou6.cloud.user.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.PageResponse;
import com.zhou6.cloud.user.dto.PostAssignDTO;
import com.zhou6.cloud.user.dto.PostChangeStatusDTO;
import com.zhou6.cloud.user.dto.PostIdDTO;
import com.zhou6.cloud.user.dto.PostQueryDTO;
import com.zhou6.cloud.user.dto.PostRemoveUserDTO;
import com.zhou6.cloud.user.dto.PostSaveDTO;
import com.zhou6.cloud.user.dto.PostUserQueryDTO;
import com.zhou6.cloud.user.dto.PostUserVO;
import com.zhou6.cloud.user.dto.PostVO;
import com.zhou6.cloud.user.entity.SysOrganization;
import com.zhou6.cloud.user.entity.SysPost;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.entity.SysUserPost;
import com.zhou6.cloud.user.mapper.SysOrganizationMapper;
import com.zhou6.cloud.user.mapper.SysPostMapper;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.mapper.SysUserPostMapper;
import com.zhou6.cloud.user.service.PostService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 岗位管理业务实现，负责岗位维护和用户-岗位-部门三元关系维护。
 */
@Service
public class PostServiceImpl implements PostService {

    private static final short STATUS_ENABLED = 1;
    private static final short STATUS_DISABLED = 0;

    private final SysPostMapper postMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper organizationMapper;
    private final StringRedisTemplate redisTemplate;

    public PostServiceImpl(SysPostMapper postMapper, SysUserPostMapper userPostMapper, SysUserMapper userMapper,
            SysOrganizationMapper organizationMapper, StringRedisTemplate redisTemplate) {
        this.postMapper = postMapper;
        this.userPostMapper = userPostMapper;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询岗位。
     *
     * @param dto 查询条件
     * @return 岗位分页结果
     */
    @Override
    public PageResponse<PostVO> page(PostQueryDTO dto) {
        PostQueryDTO query = dto == null ? new PostQueryDTO() : dto;
        Page<SysPost> page = postMapper.selectPage(Page.of(pageNum(query), pageSize(query)), new LambdaQueryWrapper<SysPost>()
                .like(hasText(query.getPostCode()), SysPost::getPostCode, query.getPostCode())
                .like(hasText(query.getPostName()), SysPost::getPostName, query.getPostName())
                .eq(query.getStatus() != null, SysPost::getStatus,
                        query.getStatus() == null ? null : query.getStatus().shortValue())
                .orderByAsc(SysPost::getSortOrder)
                .orderByDesc(SysPost::getCreateTime)
                .orderByDesc(SysPost::getId));
        return new PageResponse<>(page.getTotal(), pageNum(query), pageSize(query),
                page.getRecords().stream().map(this::toPostVo).toList());
    }

    /**
     * 新增岗位，岗位编码做全局唯一校验。
     *
     * @param dto 岗位保存参数
     */
    @Override
    public void add(PostSaveDTO dto) {
        require(dto != null, "岗位参数不能为空");
        require(hasText(dto.getPostCode()), "岗位编码不能为空");
        require(hasText(dto.getPostName()), "岗位名称不能为空");
        checkPostCodeUnique(dto.getPostCode());
        SysPost post = new SysPost();
        post.setPostCode(dto.getPostCode());
        post.setPostName(dto.getPostName());
        post.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        post.setStatus(STATUS_ENABLED);
        postMapper.insert(post);
    }

    /**
     * 修改岗位；为避免权限编码失效，忽略 postCode 修改。
     *
     * @param dto 岗位保存参数
     */
    @Override
    public void edit(PostSaveDTO dto) {
        require(dto != null && hasText(dto.getId()), "岗位ID不能为空");
        require(hasText(dto.getPostName()), "岗位名称不能为空");
        Long postId = parseRequiredId(dto.getId(), "岗位ID不正确");
        getRequiredPost(postId);
        postMapper.update(null, new LambdaUpdateWrapper<SysPost>()
                .eq(SysPost::getId, postId)
                .set(SysPost::getPostName, dto.getPostName())
                .set(SysPost::getSortOrder, dto.getSortOrder() == null ? 0 : dto.getSortOrder()));
    }

    /**
     * 删除岗位；如果岗位已分配给用户，则禁止删除。
     *
     * @param dto 岗位 ID 参数
     */
    @Override
    public void delete(PostIdDTO dto) {
        Long postId = requirePostId(dto == null ? null : dto.getId());
        long usedCount = userPostMapper.selectCount(new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getPostId, postId));
        require(usedCount == 0, "该岗位下存在用户，无法删除");
        postMapper.deleteById(postId);
    }

    /**
     * 启停岗位；停用时清理已分配用户的权限缓存。
     *
     * @param dto 状态参数
     */
    @Override
    public void changeStatus(PostChangeStatusDTO dto) {
        require(dto != null && hasText(dto.getId()), "岗位ID不能为空");
        require(dto.getStatus() != null, "岗位状态不能为空");
        Long postId = parseRequiredId(dto.getId(), "岗位ID不正确");
        getRequiredPost(postId);
        postMapper.update(null, new LambdaUpdateWrapper<SysPost>()
                .eq(SysPost::getId, postId)
                .set(SysPost::getStatus, dto.getStatus().shortValue()));
        if (Objects.equals(dto.getStatus().shortValue(), STATUS_DISABLED)) {
            clearPermissionCache(assignedUserIds(postId));
        }
    }

    /**
     * 查询岗位下的人员，可按部门过滤。
     *
     * @param dto 查询参数
     * @return 岗位用户列表
     */
    @Override
    public List<PostUserVO> users(PostUserQueryDTO dto) {
        Long postId = requirePostId(dto == null ? null : dto.getPostId());
        Long orgId = parseNullableId(dto == null ? null : dto.getOrgId(), "部门ID不正确");
        LambdaQueryWrapper<SysUserPost> wrapper = new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getPostId, postId)
                .eq(orgId != null, SysUserPost::getOrgId, orgId);
        List<SysUserPost> relations = userPostMapper.selectList(wrapper);
        if (relations.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(relations.stream()
                        .map(SysUserPost::getUserId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, String> orgNames = organizationNameMap(relations);
        return relations.stream()
                .map(relation -> toPostUserVo(relation, userMap.get(relation.getUserId()), orgNames))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 批量为用户分配岗位。
     *
     * @param dto 岗位分配参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(PostAssignDTO dto) {
        require(dto != null, "岗位分配参数不能为空");
        Long postId = requirePostId(dto.getPostId());
        Long orgId = parseRequiredId(dto.getOrgId(), "部门ID不能为空");
        require(dto.getUserIds() != null && !dto.getUserIds().isEmpty(), "分配的用户不能为空");
        require(organizationMapper.selectById(orgId) != null, "部门不存在");
        require(Objects.equals(getRequiredPost(postId).getStatus(), STATUS_ENABLED), "岗位已停用，无法分配");

        for (String userIdValue : dto.getUserIds()) {
            Long userId = parseRequiredId(userIdValue, "用户ID不正确");
            require(userMapper.selectById(userId) != null, "用户不存在");
            if (!relationExists(userId, postId, orgId)) {
                SysUserPost relation = new SysUserPost();
                relation.setUserId(userId);
                relation.setPostId(postId);
                relation.setOrgId(orgId);
                userPostMapper.insert(relation);
            }
            clearPermissionCache(userId);
        }
    }

    /**
     * 取消用户岗位。
     *
     * @param dto 取消岗位参数
     */
    @Override
    public void removeUser(PostRemoveUserDTO dto) {
        require(dto != null, "取消岗位参数不能为空");
        Long postId = requirePostId(dto.getPostId());
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        Long orgId = parseRequiredId(dto.getOrgId(), "部门ID不能为空");
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getPostId, postId)
                .eq(SysUserPost::getUserId, userId)
                .eq(SysUserPost::getOrgId, orgId));
        clearPermissionCache(userId);
    }

    private boolean relationExists(Long userId, Long postId, Long orgId) {
        return userPostMapper.selectCount(new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getUserId, userId)
                .eq(SysUserPost::getPostId, postId)
                .eq(SysUserPost::getOrgId, orgId)) > 0;
    }

    private void checkPostCodeUnique(String postCode) {
        SysPost post = postMapper.selectOne(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostCode, postCode)
                .last("limit 1"));
        require(post == null, "岗位编码已存在");
    }

    private List<Long> assignedUserIds(Long postId) {
        return userPostMapper.selectList(new LambdaQueryWrapper<SysUserPost>()
                        .eq(SysUserPost::getPostId, postId))
                .stream()
                .map(SysUserPost::getUserId)
                .distinct()
                .toList();
    }

    private void clearPermissionCache(List<Long> userIds) {
        for (Long userId : userIds) {
            clearPermissionCache(userId);
        }
    }

    private void clearPermissionCache(Long userId) {
        String userIdValue = String.valueOf(userId);
        redisTemplate.delete("zhou6:user:permissions:" + userIdValue);
        redisTemplate.delete("zhou6:user:permission:" + userIdValue);
    }

    private SysPost getRequiredPost(Long postId) {
        SysPost post = postMapper.selectById(postId);
        require(post != null, "岗位不存在");
        return post;
    }

    private Long requirePostId(String value) {
        return parseRequiredId(value, "岗位ID不能为空");
    }

    private Map<Long, String> organizationNameMap(List<SysUserPost> relations) {
        List<Long> orgIds = relations.stream()
                .map(SysUserPost::getOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (orgIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> names = new HashMap<>();
        for (SysOrganization organization : organizationMapper.selectBatchIds(orgIds)) {
            names.put(organization.getId(), organization.getOrgName());
        }
        return names;
    }

    private PostVO toPostVo(SysPost post) {
        PostVO vo = new PostVO();
        vo.setId(String.valueOf(post.getId()));
        vo.setPostCode(post.getPostCode());
        vo.setPostName(post.getPostName());
        vo.setSortOrder(post.getSortOrder());
        vo.setStatus(post.getStatus() == null ? null : post.getStatus().intValue());
        return vo;
    }

    private PostUserVO toPostUserVo(SysUserPost relation, SysUser user, Map<Long, String> orgNames) {
        if (user == null) {
            return null;
        }
        PostUserVO vo = new PostUserVO();
        vo.setUserId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setContactPhone(user.getContactPhone());
        vo.setOrgId(relation.getOrgId() == null ? null : String.valueOf(relation.getOrgId()));
        vo.setOrgName(relation.getOrgId() == null ? null : orgNames.get(relation.getOrgId()));
        return vo;
    }

    private long pageNum(PostQueryDTO dto) {
        return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
    }

    private long pageSize(PostQueryDTO dto) {
        return dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
    }

    private Long parseRequiredId(String value, String message) {
        require(hasText(value), message);
        return parseNullableId(value, message);
    }

    private Long parseNullableId(String value, String message) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
