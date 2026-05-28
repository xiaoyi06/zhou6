package com.zhou6.cloud.user.service.impl;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.PageResponse;
import com.zhou6.cloud.user.dto.UserChangeStatusDTO;
import com.zhou6.cloud.user.dto.UserDeleteDTO;
import com.zhou6.cloud.user.dto.UserIdDTO;
import com.zhou6.cloud.user.dto.UserManageVO;
import com.zhou6.cloud.user.dto.UserQueryDTO;
import com.zhou6.cloud.user.dto.UserSaveDTO;
import com.zhou6.cloud.user.entity.SysOrganization;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.entity.SysUserOrganization;
import com.zhou6.cloud.user.mapper.SysOrganizationMapper;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.mapper.SysUserOrganizationMapper;
import com.zhou6.cloud.user.service.UserManagementService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户管理业务实现，保证用户主部门字段和用户组织中间表一致。
 */
@Service
public class UserManagementServiceImpl implements UserManagementService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String DEFAULT_AVATAR = "xxx.fileid";
    private static final short STATUS_ENABLED = 1;
    private static final short STATUS_DISABLED = 0;
    private static final short LOCKED_NO = 0;
    private static final short PRIMARY_NO = 0;
    private static final short PRIMARY_YES = 1;

    private final SysUserMapper userMapper;
    private final SysOrganizationMapper organizationMapper;
    private final SysUserOrganizationMapper userOrganizationMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserManagementServiceImpl(SysUserMapper userMapper, SysOrganizationMapper organizationMapper,
            SysUserOrganizationMapper userOrganizationMapper, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.userOrganizationMapper = userOrganizationMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询用户；按部门筛选时通过 sys_user_organization 中间表取用户范围。
     *
     * @param dto 查询条件
     * @return 用户分页结果
     */
    @Override
    public PageResponse<UserManageVO> page(UserQueryDTO dto) {
        UserQueryDTO query = dto == null ? new UserQueryDTO() : dto;
        Long orgId = parseNullableId(query.getPrimaryOrgId(), "主部门ID不正确");
        List<Long> orgUserIds = queryOrgUserIds(orgId);
        if (orgId != null && orgUserIds.isEmpty()) {
            return new PageResponse<>(0, pageNum(query), pageSize(query), Collections.emptyList());
        }

        LambdaQueryWrapper<SysUser> wrapper = buildUserQuery(query);
        if (orgId != null) {
            wrapper.in(SysUser::getId, orgUserIds);
        }
        Page<SysUser> page = userMapper.selectPage(Page.of(pageNum(query), pageSize(query)), wrapper);
        return new PageResponse<>(page.getTotal(), pageNum(query), pageSize(query), toVos(page.getRecords()));
    }

    /**
     * 新增用户并同步主部门关系。
     *
     * @param dto 用户保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UserSaveDTO dto) {
        require(dto != null, "用户参数不能为空");
        require(hasText(dto.getUsername()), "登录账号不能为空");
        checkUsernameUnique(dto.getUsername(), null);
        Long primaryOrgId = parseNullableId(dto.getPrimaryOrgId(), "主部门ID不正确");
        checkOrganizationExists(primaryOrgId);

        SysUser user = new SysUser();
        fillUser(user, dto, true);
        user.setPrimaryOrgId(primaryOrgId);
        user.setPassword(passwordEncoder.encode(hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD));
        user.setStatus(dto.getStatus() == null ? STATUS_ENABLED : dto.getStatus().shortValue());
        user.setIsLocked(LOCKED_NO);
        user.setFailedLoginAttempts(0);
        userMapper.insert(user);
        syncPrimaryOrganization(user.getId(), primaryOrgId);
    }

    /**
     * 修改用户并同步主部门关系。
     *
     * @param dto 用户保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(UserSaveDTO dto) {
        require(dto != null && hasText(dto.getId()), "用户ID不能为空");
        Long userId = parseRequiredId(dto.getId(), "用户ID不正确");
        SysUser user = getRequiredUser(userId);
        if (hasText(dto.getUsername())) {
            checkUsernameUnique(dto.getUsername(), userId);
        }
        Long primaryOrgId = parseNullableId(dto.getPrimaryOrgId(), "主部门ID不正确");
        checkOrganizationExists(primaryOrgId);

        fillUser(user, dto, false);
        user.setPrimaryOrgId(primaryOrgId);
        if (hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);
        syncPrimaryOrganization(userId, primaryOrgId);
    }

    /**
     * 删除用户，同时清理组织关系和 Redis 登录会话。
     *
     * @param dto 删除参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserDeleteDTO dto) {
        require(dto != null, "用户ID不能为空");
        List<String> deleteIds = deleteIds(dto);
        require(!deleteIds.isEmpty(), "用户ID不能为空");
        List<Long> userIds = deleteIds.stream()
                .map(id -> parseRequiredId(id, "用户ID不正确"))
                .toList();
        userMapper.deleteBatchIds(userIds);
        userOrganizationMapper.delete(new LambdaQueryWrapper<SysUserOrganization>()
                .in(SysUserOrganization::getUserId, userIds));
        userIds.forEach(this::kickOutUser);
    }

    /**
     * 启停用户；禁用时立即清理当前登录会话。
     *
     * @param dto 状态参数
     */
    @Override
    public void changeStatus(UserChangeStatusDTO dto) {
        require(dto != null && hasText(dto.getId()), "用户ID不能为空");
        require(dto.getStatus() != null, "用户状态不能为空");
        Long userId = parseRequiredId(dto.getId(), "用户ID不正确");
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getStatus, dto.getStatus().shortValue()));
        if (Objects.equals(dto.getStatus().shortValue(), STATUS_DISABLED)) {
            kickOutUser(userId);
        }
    }

    /**
     * 查询用户详情。
     *
     * @param dto 用户 ID 参数
     * @return 用户详情
     */
    @Override
    public UserManageVO getById(UserIdDTO dto) {
        require(dto != null && hasText(dto.getId()), "用户ID不能为空");
        return toVo(getRequiredUser(parseRequiredId(dto.getId(), "用户ID不正确")), organizationNameMap());
    }

    private void fillUser(SysUser user, UserSaveDTO dto, boolean add) {
        if (add || hasText(dto.getUsername())) {
            user.setUsername(dto.getUsername());
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setContactPhone(dto.getContactPhone());
        user.setGender(dto.getGender() == null ? null : dto.getGender().shortValue());
        user.setAvatar(hasText(dto.getAvatar()) ? dto.getAvatar() : DEFAULT_AVATAR);
        user.setPersonalSignature(dto.getPersonalSignature());
        user.setWorkStatus(dto.getWorkStatus());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus().shortValue());
        }
    }

    private LambdaQueryWrapper<SysUser> buildUserQuery(UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(hasText(query.getContactPhone()), SysUser::getContactPhone, query.getContactPhone())
                .eq(query.getStatus() != null, SysUser::getStatus,
                        query.getStatus() == null ? null : query.getStatus().shortValue())
                .orderByDesc(SysUser::getCreateTime)
                .orderByDesc(SysUser::getId);
        return wrapper;
    }

    private List<Long> queryOrgUserIds(Long orgId) {
        if (orgId == null) {
            return Collections.emptyList();
        }
        return userOrganizationMapper.selectList(new LambdaQueryWrapper<SysUserOrganization>()
                        .eq(SysUserOrganization::getOrgId, orgId))
                .stream()
                .map(SysUserOrganization::getUserId)
                .toList();
    }

    private void syncPrimaryOrganization(Long userId, Long primaryOrgId) {
        userOrganizationMapper.update(null, new LambdaUpdateWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getUserId, userId)
                .set(SysUserOrganization::getIsPrimary, PRIMARY_NO));
        if (primaryOrgId == null) {
            return;
        }
        SysUserOrganization relation = userOrganizationMapper.selectOne(new LambdaQueryWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getUserId, userId)
                .eq(SysUserOrganization::getOrgId, primaryOrgId)
                .last("limit 1"));
        if (relation == null) {
            relation = new SysUserOrganization();
            relation.setUserId(userId);
            relation.setOrgId(primaryOrgId);
            relation.setIsPrimary(PRIMARY_YES);
            userOrganizationMapper.insert(relation);
            return;
        }
        userOrganizationMapper.update(null, new LambdaUpdateWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getUserId, userId)
                .eq(SysUserOrganization::getOrgId, primaryOrgId)
                .set(SysUserOrganization::getIsPrimary, PRIMARY_YES));
    }

    private void checkUsernameUnique(String username, Long selfId) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        require(user == null || Objects.equals(user.getId(), selfId), "登录账号已存在");
    }

    private void checkOrganizationExists(Long orgId) {
        if (orgId == null) {
            return;
        }
        require(organizationMapper.selectById(orgId) != null, "主部门不存在");
    }

    private SysUser getRequiredUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        require(user != null, "用户不存在");
        return user;
    }

    private void kickOutUser(Long userId) {
        String userIdValue = String.valueOf(userId);
        String currentRefreshToken = redisTemplate.opsForValue().get("zhou6:auth:current-refresh:" + userIdValue);
        if (hasText(currentRefreshToken)) {
            redisTemplate.delete("zhou6:auth:refresh:" + currentRefreshToken);
        }
        redisTemplate.delete("zhou6:auth:session:" + userIdValue);
        redisTemplate.delete("zhou6:auth:current-refresh:" + userIdValue);
        redisTemplate.delete("zhou6:auth:login-ip:" + userIdValue);
    }

    private List<UserManageVO> toVos(List<SysUser> users) {
        Map<Long, String> organizationNames = organizationNameMap();
        return users.stream().map(user -> toVo(user, organizationNames)).toList();
    }

    private Map<Long, String> organizationNameMap() {
        List<SysOrganization> organizations = organizationMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                .select(SysOrganization::getId, SysOrganization::getOrgName));
        Map<Long, String> names = new HashMap<>();
        for (SysOrganization organization : organizations) {
            names.put(organization.getId(), organization.getOrgName());
        }
        return names;
    }

    private UserManageVO toVo(SysUser user, Map<Long, String> organizationNames) {
        UserManageVO vo = new UserManageVO();
        vo.setId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setContactPhone(user.getContactPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender() == null ? null : user.getGender().intValue());
        vo.setPrimaryOrgId(user.getPrimaryOrgId() == null ? null : String.valueOf(user.getPrimaryOrgId()));
        vo.setPrimaryOrgName(user.getPrimaryOrgId() == null ? null : organizationNames.get(user.getPrimaryOrgId()));
        vo.setAvatar(user.getAvatar());
        vo.setPersonalSignature(user.getPersonalSignature());
        vo.setWorkStatus(user.getWorkStatus());
        vo.setStatus(user.getStatus() == null ? null : user.getStatus().intValue());
        return vo;
    }

    private long pageNum(UserQueryDTO dto) {
        return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
    }

    private long pageSize(UserQueryDTO dto) {
        return dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
    }

    private Long parseRequiredId(String value, String message) {
        require(hasText(value), message);
        return parseNullableId(value, message);
    }

    private List<String> deleteIds(UserDeleteDTO dto) {
        List<String> values = new ArrayList<>();
        if (hasText(dto.getId())) {
            values.add(dto.getId());
        }
        if (dto.getIds() != null) {
            values.addAll(dto.getIds());
        }
        return values;
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
