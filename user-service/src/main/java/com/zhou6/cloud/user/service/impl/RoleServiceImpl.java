package com.zhou6.cloud.user.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.dto.RoleAssignUsersDTO;
import com.zhou6.cloud.user.dto.RoleChangeStatusDTO;
import com.zhou6.cloud.user.dto.RoleDataScopeDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleQueryDTO;
import com.zhou6.cloud.user.dto.RoleRemoveUserDTO;
import com.zhou6.cloud.user.dto.RoleSaveDTO;
import com.zhou6.cloud.user.vo.RoleUserVO;
import com.zhou6.cloud.user.vo.RoleVO;
import com.zhou6.cloud.user.entity.SysOrganization;
import com.zhou6.cloud.user.entity.SysRole;
import com.zhou6.cloud.user.entity.SysRoleOrg;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.entity.SysUserRole;
import com.zhou6.cloud.user.mapper.SysOrganizationMapper;
import com.zhou6.cloud.user.mapper.SysRoleMapper;
import com.zhou6.cloud.user.mapper.SysRoleOrgMapper;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.mapper.SysUserRoleMapper;
import com.zhou6.cloud.user.service.RoleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色管理业务实现，负责维护角色、用户授权和自定义数据范围。
 */
@Service
public class RoleServiceImpl implements RoleService {


    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleOrgMapper roleOrgMapper;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper organizationMapper;
    private final StringRedisTemplate redisTemplate;

    public RoleServiceImpl(SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper,
            SysRoleOrgMapper roleOrgMapper, SysUserMapper userMapper, SysOrganizationMapper organizationMapper,
            StringRedisTemplate redisTemplate) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleOrgMapper = roleOrgMapper;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询角色。
     *
     * @param dto 查询条件
     * @return 角色分页结果
     */
    @Override
    public PageResponse<RoleVO> page(RoleQueryDTO dto) {
        RoleQueryDTO query = dto == null ? new RoleQueryDTO() : dto;
        Page<SysRole> page = roleMapper.selectPage(Page.of(pageNum(query), pageSize(query)),
                new LambdaQueryWrapper<SysRole>()
                        .like(hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                        .like(hasText(query.getRoleCode()), SysRole::getRoleCode, query.getRoleCode())
                        .eq(query.getStatus() != null, SysRole::getStatus,
                                query.getStatus() == null ? null : query.getStatus().shortValue())
                        .orderByAsc(SysRole::getSortOrder)
                        .orderByDesc(SysRole::getCreateTime)
                        .orderByDesc(SysRole::getId));
        return new PageResponse<>(page.getTotal(), pageNum(query), pageSize(query),
                page.getRecords().stream().map(this::toRoleVo).toList());
    }

    /**
     * 新增角色，校验角色编码全局唯一。
     *
     * @param dto 角色保存参数
     */
    @Override
    public void add(RoleSaveDTO dto) {
        require(dto != null, "角色参数不能为空");
        require(hasText(dto.getRoleName()), "角色名称不能为空");
        require(hasText(dto.getRoleCode()), "角色编码不能为空");
        require(dto.getDataScope() != null, "数据权限范围不能为空");
        checkRoleCodeUnique(dto.getRoleCode());
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDataScope(dto.getDataScope().shortValue());
        role.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        role.setStatus(StatusConstants.STATUS_ENABLED);
        role.setRemark(dto.getRemark());
        roleMapper.insert(role);
    }

    /**
     * 修改角色；为避免权限编码失效，忽略 roleCode 修改。
     *
     * @param dto 角色保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(RoleSaveDTO dto) {
        require(dto != null && hasText(dto.getId()), "角色ID不能为空");
        require(hasText(dto.getRoleName()), "角色名称不能为空");
        require(dto.getDataScope() != null, "数据权限范围不能为空");
        Long roleId = parseRequiredId(dto.getId(), "角色ID不正确");
        SysRole oldRole = getRequiredRole(roleId);
        roleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .set(SysRole::getRoleName, dto.getRoleName())
                .set(SysRole::getDataScope, dto.getDataScope().shortValue())
                .set(SysRole::getSortOrder, dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                .set(SysRole::getRemark, dto.getRemark()));
        if (!Objects.equals(oldRole.getDataScope(), dto.getDataScope().shortValue())) {
            roleOrgMapper.delete(new LambdaQueryWrapper<SysRoleOrg>().eq(SysRoleOrg::getRoleId, roleId));
            clearPermissionCache(assignedUserIds(roleId));
        }
    }

    /**
     * 删除角色；存在关联用户时禁止删除。
     *
     * @param dto 角色 ID 参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(RoleIdDTO dto) {
        Long roleId = requireRoleId(dto == null ? null : dto.getId());
        long usedCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId));
        require(usedCount == 0, "存在关联用户，不允许删除");
        roleMapper.deleteById(roleId);
        roleOrgMapper.delete(new LambdaQueryWrapper<SysRoleOrg>().eq(SysRoleOrg::getRoleId, roleId));
    }

    /**
     * 启停角色；停用时清理相关用户权限缓存。
     *
     * @param dto 状态参数
     */
    @Override
    public void changeStatus(RoleChangeStatusDTO dto) {
        require(dto != null && hasText(dto.getId()), "角色ID不能为空");
        require(dto.getStatus() != null, "角色状态不能为空");
        Long roleId = parseRequiredId(dto.getId(), "角色ID不正确");
        getRequiredRole(roleId);
        roleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .set(SysRole::getStatus, dto.getStatus().shortValue()));
        if (Objects.equals(dto.getStatus().shortValue(), StatusConstants.STATUS_DISABLED)) {
            clearPermissionCache(assignedUserIds(roleId));
        }
    }

    /**
     * 查询角色下的用户。
     *
     * @param dto 角色 ID 参数
     * @return 角色用户列表
     */
    @Override
    public List<RoleUserVO> users(RoleIdDTO dto) {
        Long roleId = requireRoleId(dto == null ? null : dto.getId());
        List<SysUserRole> relations = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId));
        if (relations.isEmpty()) {
            return List.of();
        }
        return userMapper.selectBatchIds(relations.stream()
                        .map(SysUserRole::getUserId)
                        .distinct()
                        .toList())
                .stream()
                .map(this::toRoleUserVo)
                .toList();
    }

    /**
     * 批量给角色分配用户。
     *
     * @param dto 分配用户参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(RoleAssignUsersDTO dto) {
        require(dto != null, "角色分配参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        require(dto.getUserIds() != null && !dto.getUserIds().isEmpty(), "分配的用户不能为空");
        require(Objects.equals(getRequiredRole(roleId).getStatus(), StatusConstants.STATUS_ENABLED), "角色已停用，无法分配");
        for (String userIdValue : dto.getUserIds()) {
            Long userId = parseRequiredId(userIdValue, "用户ID不正确");
            require(userMapper.selectById(userId) != null, "用户不存在");
            if (!relationExists(userId, roleId)) {
                SysUserRole relation = new SysUserRole();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                userRoleMapper.insert(relation);
            }
            clearPermissionCache(userId);
        }
    }

    /**
     * 取消用户角色。
     *
     * @param dto 取消用户参数
     */
    @Override
    public void removeUser(RoleRemoveUserDTO dto) {
        require(dto != null, "取消角色参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)
                .eq(SysUserRole::getUserId, userId));
        clearPermissionCache(userId);
    }

    /**
     * 配置角色数据权限范围；自定义范围会重建 sys_role_org 数据。
     *
     * @param dto 数据权限配置参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configDataScope(RoleDataScopeDTO dto) {
        require(dto != null, "数据权限参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        require(dto.getDataScope() != null, "数据权限范围不能为空");
        short dataScope = dto.getDataScope().shortValue();
        if (dataScope == StatusConstants.DATA_SCOPE_CUSTOM) {
            require(dto.getOrgIds() != null && !dto.getOrgIds().isEmpty(), "自定义数据权限机构不能为空");
        }
        roleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .set(SysRole::getDataScope, dataScope));
        roleOrgMapper.delete(new LambdaQueryWrapper<SysRoleOrg>().eq(SysRoleOrg::getRoleId, roleId));
        if (dataScope == StatusConstants.DATA_SCOPE_CUSTOM) {
            insertRoleOrgs(roleId, dto.getOrgIds());
        }
        clearPermissionCache(assignedUserIds(roleId));
    }

    private void insertRoleOrgs(Long roleId, List<String> orgIds) {
        for (String orgIdValue : orgIds) {
            Long orgId = parseRequiredId(orgIdValue, "机构ID不正确");
            SysOrganization organization = organizationMapper.selectById(orgId);
            require(organization != null, "机构不存在");
            SysRoleOrg roleOrg = new SysRoleOrg();
            roleOrg.setRoleId(roleId);
            roleOrg.setOrgId(orgId);
            roleOrgMapper.insert(roleOrg);
        }
    }

    private boolean relationExists(Long userId, Long roleId) {
        return userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId)) > 0;
    }

    private void checkRoleCodeUnique(String roleCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .last("limit 1"));
        require(role == null, "角色编码已存在");
    }

    private List<Long> assignedUserIds(Long roleId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId))
                .stream()
                .map(SysUserRole::getUserId)
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
        redisTemplate.delete("zhou6:user:data-scope:" + userIdValue);
    }

    private SysRole getRequiredRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        require(role != null, "角色不存在");
        return role;
    }

    private Long requireRoleId(String value) {
        return parseRequiredId(value, "角色ID不能为空");
    }

    private RoleVO toRoleVo(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(String.valueOf(role.getId()));
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDataScope(role.getDataScope() == null ? null : role.getDataScope().intValue());
        vo.setSortOrder(role.getSortOrder());
        vo.setStatus(role.getStatus() == null ? null : role.getStatus().intValue());
        vo.setRemark(role.getRemark());
        return vo;
    }

    private RoleUserVO toRoleUserVo(SysUser user) {
        RoleUserVO vo = new RoleUserVO();
        vo.setUserId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setContactPhone(user.getContactPhone());
        return vo;
    }

    private long pageNum(RoleQueryDTO dto) {
        return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
    }

    private long pageSize(RoleQueryDTO dto) {
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
