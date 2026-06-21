package com.zhou6.cloud.user.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.dto.MenuAssignDTO;
import com.zhou6.cloud.user.dto.RoleAssignUsersDTO;
import com.zhou6.cloud.user.dto.RoleChangeStatusDTO;
import com.zhou6.cloud.user.dto.RoleDataScopeDTO;
import com.zhou6.cloud.user.dto.RoleIdDTO;
import com.zhou6.cloud.user.dto.RoleMenuQueryDTO;
import com.zhou6.cloud.user.dto.RoleQueryDTO;
import com.zhou6.cloud.user.dto.RoleRemoveUserDTO;
import com.zhou6.cloud.user.dto.RoleSaveDTO;
import com.zhou6.cloud.user.dto.RoleUserQueryDTO;
import com.zhou6.cloud.user.vo.MenuVO;
import com.zhou6.cloud.user.vo.RoleUserVO;
import com.zhou6.cloud.user.vo.RoleVO;
import com.zhou6.cloud.user.entity.SysMenu;
import com.zhou6.cloud.user.entity.SysExternalSystem;
import com.zhou6.cloud.user.entity.SysOrganization;
import com.zhou6.cloud.user.entity.SysRole;
import com.zhou6.cloud.user.entity.SysRoleMenu;
import com.zhou6.cloud.user.entity.SysRoleOrg;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.entity.SysUserRole;
import com.zhou6.cloud.user.mapper.SysMenuMapper;
import com.zhou6.cloud.user.mapper.SysExternalSystemMapper;
import com.zhou6.cloud.user.mapper.SysOrganizationMapper;
import com.zhou6.cloud.user.mapper.SysRoleMapper;
import com.zhou6.cloud.user.mapper.SysRoleMenuMapper;
import com.zhou6.cloud.user.mapper.SysRoleOrgMapper;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.mapper.SysUserRoleMapper;
import com.zhou6.cloud.user.service.RoleMenuRelationService;
import com.zhou6.cloud.user.service.RoleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色管理业务实现，负责维护角色、用户授权和自定义数据范围。
 */
@Service
public class RoleServiceImpl implements RoleService {


    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long ROOT_PARENT_ID = 0L;

    private final SysRoleMapper roleMapper;
    private final SysExternalSystemMapper externalSystemMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleOrgMapper roleOrgMapper;
    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysOrganizationMapper organizationMapper;
    private final RoleMenuRelationService roleMenuRelationService;
    private final StringRedisTemplate redisTemplate;

    public RoleServiceImpl(SysRoleMapper roleMapper, SysExternalSystemMapper externalSystemMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMenuMapper roleMenuMapper, SysRoleOrgMapper roleOrgMapper, SysUserMapper userMapper,
            SysMenuMapper menuMapper, SysOrganizationMapper organizationMapper,
            RoleMenuRelationService roleMenuRelationService, StringRedisTemplate redisTemplate) {
        this.roleMapper = roleMapper;
        this.externalSystemMapper = externalSystemMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleOrgMapper = roleOrgMapper;
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.organizationMapper = organizationMapper;
        this.roleMenuRelationService = roleMenuRelationService;
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
                        .eq(hasText(query.getSystemId()), SysRole::getSystemId,
                                parseNullableId(query.getSystemId(), "所属系统ID不正确"))
                        .eq(query.getStatus() != null, SysRole::getStatus,
                                query.getStatus() == null ? null : query.getStatus().shortValue())
                        .orderByAsc(SysRole::getSortOrder)
                        .orderByDesc(SysRole::getCreateTime)
                        .orderByDesc(SysRole::getId));
        return new PageResponse<>(page.getTotal(), pageNum(query), pageSize(query),
                toRoleVos(page.getRecords()));
    }

    /**
     * 新增角色，校验角色编码全局唯一，并在自定义数据范围时保存机构关联。
     *
     * @param dto 角色保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoleSaveDTO dto) {
        require(dto != null, "角色参数不能为空");
        require(hasText(dto.getRoleName()), "角色名称不能为空");
        require(hasText(dto.getRoleCode()), "角色编码不能为空");
        Long systemId = parseRequiredId(dto.getSystemId(), "所属系统不能为空");
        require(dto.getDataScope() != null, "数据权限范围不能为空");
        checkRoleCodeUnique(dto.getRoleCode());
        requireEnabledExternalSystem(systemId);
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setSystemId(systemId);
        role.setDataScope(dto.getDataScope().shortValue());
        role.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        role.setStatus(StatusConstants.STATUS_ENABLED);
        role.setRemark(dto.getRemark());
        roleMapper.insert(role);
        syncRoleOrgs(role.getId(), dto.getDataScope().shortValue(), dto.getOrgIds());
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
        Long systemId = parseRequiredId(dto.getSystemId(), "所属系统不能为空");
        require(dto.getDataScope() != null, "数据权限范围不能为空");
        Long roleId = parseRequiredId(dto.getId(), "角色ID不正确");
        getRequiredRole(roleId);
        requireEnabledExternalSystem(systemId);
        roleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .set(SysRole::getRoleName, dto.getRoleName())
                .set(SysRole::getSystemId, systemId)
                .set(SysRole::getDataScope, dto.getDataScope().shortValue())
                .set(SysRole::getSortOrder, dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                .set(SysRole::getRemark, dto.getRemark()));
        syncRoleOrgs(roleId, dto.getDataScope().shortValue(), dto.getOrgIds());
        clearPermissionCache(assignedUserIds(roleId));
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

    @Override
    public List<RoleVO> listAll() {
        return toRoleVos(roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .orderByAsc(SysRole::getSortOrder)
                        .orderByDesc(SysRole::getCreateTime)
                        .orderByDesc(SysRole::getId)));
    }

    /**
     * 分页查询角色下的用户。
     *
     * @param dto 查询参数
     * @return 角色用户分页列表
     */
    @Override
    public PageResponse<RoleUserVO> users(RoleUserQueryDTO dto) {
        RoleUserQueryDTO query = dto == null ? new RoleUserQueryDTO() : dto;
        Long roleId = requireRoleId(hasText(query.getRoleId()) ? query.getRoleId() : query.getId());
        List<SysUserRole> relations = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId));
        if (relations.isEmpty()) {
            return new PageResponse<>(0, pageNum(query), pageSize(query), List.of());
        }
        Page<SysUser> page = userMapper.selectPage(Page.of(pageNum(query), pageSize(query)),
                buildRoleUserQuery(query, relations.stream()
                        .map(SysUserRole::getUserId)
                        .distinct()
                        .toList()));
        Map<Long, String> orgNames = organizationNameMap(page.getRecords());
        return new PageResponse<>(page.getTotal(), pageNum(query), pageSize(query),
                page.getRecords().stream()
                        .map(user -> toRoleUserVo(user, orgNames))
                        .toList());
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
        List<Long> userIds = dto.getUserIds().stream()
                .map(userIdValue -> parseRequiredId(userIdValue, "用户ID不正确"))
                .distinct()
                .toList();
        List<SysUserRole> relations = new ArrayList<>();
        for (Long userId : userIds) {
            require(userMapper.selectById(userId) != null, "用户不存在");
            if (!relationExists(userId, roleId)) {
                SysUserRole relation = new SysUserRole();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                relations.add(relation);
            }
            clearPermissionCache(userId);
        }
        if (!relations.isEmpty()) {
            userRoleMapper.insertBatch(relations);
        }
    }

    /**
     * 取消用户角色。
     *
     * @param dto 取消用户参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(RoleRemoveUserDTO dto) {
        require(dto != null, "取消角色参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        require(dto.getUserIds() != null && !dto.getUserIds().isEmpty(), "用户ID不能为空");
        List<Long> userIds = dto.getUserIds().stream()
                .map(userId -> parseRequiredId(userId, "用户ID不正确"))
                .distinct()
                .toList();
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));
        clearPermissionCache(userIds);
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
        getRequiredRole(roleId);
        require(dto.getDataScope() != null, "数据权限范围不能为空");
        short dataScope = dto.getDataScope().shortValue();
        roleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .set(SysRole::getDataScope, dataScope));
        syncRoleOrgs(roleId, dataScope, dto.getOrgIds());
        clearPermissionCache(assignedUserIds(roleId));
    }

    /**
     * 批量新增角色菜单；只追加不存在的关系，不清空原配置。
     *
     * @param dto 角色菜单参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenus(MenuAssignDTO dto) {
        require(dto != null, "角色菜单参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        getRequiredRole(roleId);
        require(dto.getMenuIds() != null && !dto.getMenuIds().isEmpty(), "菜单不能为空");
        List<Long> menuIds = dto.getMenuIds().stream()
                .map(menuIdValue -> parseRequiredId(menuIdValue, "菜单ID不正确"))
                .distinct()
                .toList();
        List<SysRoleMenu> relations = new ArrayList<>();
        for (Long menuId : menuIds) {
            require(menuMapper.selectById(menuId) != null, "菜单不存在");
            if (!roleMenuExists(roleId, menuId)) {
                SysRoleMenu relation = new SysRoleMenu();
                relation.setRoleId(roleId);
                relation.setMenuId(menuId);
                relations.add(relation);
            }
        }
        if (!relations.isEmpty()) {
            roleMenuMapper.insertBatch(relations);
        }
        clearPermissionCache(assignedUserIds(roleId));
    }

    /**
     * 覆盖保存角色菜单，允许提交空菜单列表以清空角色菜单权限。
     *
     * @param dto 角色菜单参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(MenuAssignDTO dto) {
        require(dto != null, "角色菜单参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        getRequiredRole(roleId);
        List<Long> menuIds = dto.getMenuIds() == null ? List.of() : dto.getMenuIds().stream()
                .map(menuIdValue -> parseRequiredId(menuIdValue, "菜单ID不正确"))
                .distinct()
                .toList();
        for (Long menuId : menuIds) {
            require(menuMapper.selectById(menuId) != null, "菜单不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (!menuIds.isEmpty()) {
            List<SysRoleMenu> relations = menuIds.stream().map(menuId -> {
                SysRoleMenu relation = new SysRoleMenu();
                relation.setRoleId(roleId);
                relation.setMenuId(menuId);
                return relation;
            }).toList();
            roleMenuMapper.insertBatch(relations);
        }
        clearPermissionCache(assignedUserIds(roleId));
    }

    /**
     * 批量移除角色菜单。
     *
     * @param dto 角色菜单参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMenus(MenuAssignDTO dto) {
        require(dto != null, "角色菜单参数不能为空");
        Long roleId = requireRoleId(dto.getRoleId());
        getRequiredRole(roleId);
        require(dto.getMenuIds() != null && !dto.getMenuIds().isEmpty(), "菜单不能为空");
        List<Long> menuIds = dto.getMenuIds().stream()
                .map(menuIdValue -> parseRequiredId(menuIdValue, "菜单ID不正确"))
                .distinct()
                .toList();
        for (Long menuId : menuIds) {
            require(menuMapper.selectById(menuId) != null, "菜单不存在");
        }
        roleMenuRelationService.removeRoleMenus(List.of(roleId), menuIds);
        clearPermissionCache(assignedUserIds(roleId));
    }

    /**
     * 查询角色已配置菜单。
     *
     * @param dto 查询参数
     * @return 菜单树
     */
    @Override
    public List<MenuVO> menus(RoleMenuQueryDTO dto) {
        RoleMenuQueryDTO query = dto == null ? new RoleMenuQueryDTO() : dto;
        Long roleId = requireRoleId(query.getRoleId());
        getRequiredRole(roleId);
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<MenuVO> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(query.getStatus() != null, SysMenu::getStatus,
                                query.getStatus() == null ? null : query.getStatus().shortValue())
                        .orderByAsc(SysMenu::getSortOrder)
                        .orderByAsc(SysMenu::getId))
                .stream()
                .map(this::toMenuVo)
                .toList();
        return buildMenuTree(menus);
    }

    private void syncRoleOrgs(Long roleId, short dataScope, List<String> orgIds) {
        roleOrgMapper.delete(new LambdaQueryWrapper<SysRoleOrg>().eq(SysRoleOrg::getRoleId, roleId));
        if (dataScope != StatusConstants.DATA_SCOPE_CUSTOM) {
            return;
        }
        List<String> distinctOrgIds = orgIds == null ? List.of() : orgIds.stream()
                .filter(this::hasText)
                .distinct()
                .toList();
        require(!distinctOrgIds.isEmpty(), "自定义数据权限机构不能为空");
        List<SysRoleOrg> relations = new ArrayList<>();
        for (String orgIdValue : distinctOrgIds) {
            Long orgId = parseRequiredId(orgIdValue, "机构ID不正确");
            SysOrganization organization = organizationMapper.selectById(orgId);
            require(organization != null, "机构不存在");
            SysRoleOrg roleOrg = new SysRoleOrg();
            roleOrg.setRoleId(roleId);
            roleOrg.setOrgId(orgId);
            relations.add(roleOrg);
        }
        roleOrgMapper.insertBatch(relations);
    }

    private boolean relationExists(Long userId, Long roleId) {
        return userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId)) > 0;
    }

    private boolean roleMenuExists(Long roleId, Long menuId) {
        return roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getMenuId, menuId)) > 0;
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
        redisTemplate.delete("zhou6:user:routers:" + userIdValue);
    }

    private SysRole getRequiredRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        require(role != null, "角色不存在");
        return role;
    }

    private void requireEnabledExternalSystem(Long systemId) {
        SysExternalSystem system = externalSystemMapper.selectById(systemId);
        require(system != null, "所属外部系统不存在");
        require(Objects.equals(system.getStatus(), StatusConstants.STATUS_ENABLED), "所属外部系统已停用");
    }

    private Long requireRoleId(String value) {
        return parseRequiredId(value, "角色ID不能为空");
    }

    private List<RoleVO> toRoleVos(List<SysRole> roles) {
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        Map<Long, List<String>> roleOrgIds = new HashMap<>();
        if (!roleIds.isEmpty()) {
            for (SysRoleOrg roleOrg : roleOrgMapper.selectList(new LambdaQueryWrapper<SysRoleOrg>()
                    .in(SysRoleOrg::getRoleId, roleIds))) {
                roleOrgIds.computeIfAbsent(roleOrg.getRoleId(), ignored -> new ArrayList<>())
                        .add(String.valueOf(roleOrg.getOrgId()));
            }
        }
        List<Long> systemIds = roles.stream()
                .map(SysRole::getSystemId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> systemNames = new HashMap<>();
        if (!systemIds.isEmpty()) {
            for (SysExternalSystem system : externalSystemMapper.selectBatchIds(systemIds)) {
                systemNames.put(system.getId(), system.getSystemName());
            }
        }
        return roles.stream().map(role -> toRoleVo(role, systemNames, roleOrgIds)).toList();
    }

    private RoleVO toRoleVo(SysRole role, Map<Long, String> systemNames, Map<Long, List<String>> roleOrgIds) {
        RoleVO vo = new RoleVO();
        vo.setId(String.valueOf(role.getId()));
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setSystemId(role.getSystemId() == null ? null : String.valueOf(role.getSystemId()));
        vo.setSystemName(role.getSystemId() == null ? null : systemNames.get(role.getSystemId()));
        vo.setDataScope(role.getDataScope() == null ? null : role.getDataScope().intValue());
        vo.setOrgIds(roleOrgIds.getOrDefault(role.getId(), List.of()));
        vo.setSortOrder(role.getSortOrder());
        vo.setStatus(role.getStatus() == null ? null : role.getStatus().intValue());
        vo.setRemark(role.getRemark());
        return vo;
    }

    private Map<Long, String> organizationNameMap(List<SysUser> users) {
        List<Long> orgIds = users.stream()
                .map(SysUser::getPrimaryOrgId)
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

    private LambdaQueryWrapper<SysUser> buildRoleUserQuery(RoleUserQueryDTO query, List<Long> userIds) {
        return new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, userIds)
                .and(hasText(query.getKeyword()), wrapper -> wrapper
                        .likeRight(SysUser::getUsername, query.getKeyword())
                        .or()
                        .likeRight(SysUser::getNickname, query.getKeyword()))
                .likeRight(hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .likeRight(hasText(query.getNickname()), SysUser::getNickname, query.getNickname())
                .likeRight(hasText(query.getContactPhone()), SysUser::getContactPhone, query.getContactPhone())
                .likeRight(hasText(query.getEmail()), SysUser::getEmail, query.getEmail())
                .eq(query.getStatus() != null, SysUser::getStatus,
                        query.getStatus() == null ? null : query.getStatus().shortValue())
                .orderByDesc(SysUser::getCreateTime)
                .orderByDesc(SysUser::getId);
    }

    private RoleUserVO toRoleUserVo(SysUser user, Map<Long, String> orgNames) {
        RoleUserVO vo = new RoleUserVO();
        vo.setUserId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setContactPhone(user.getContactPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender() == null ? null : user.getGender().intValue());
        vo.setPrimaryOrgId(user.getPrimaryOrgId() == null ? null : String.valueOf(user.getPrimaryOrgId()));
        vo.setPrimaryOrgName(user.getPrimaryOrgId() == null ? null : orgNames.get(user.getPrimaryOrgId()));
        vo.setAvatarFileId(user.getAvatarFileId() == null ? null : String.valueOf(user.getAvatarFileId()));
        vo.setPersonalSignature(user.getPersonalSignature());
        vo.setWorkStatus(user.getWorkStatus());
        vo.setStatus(user.getStatus() == null ? null : user.getStatus().intValue());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setLastLoginTime(user.getLastLoginTime() == null ? null : user.getLastLoginTime().format(DTF));
        vo.setCreateTime(user.getCreateTime() == null ? null : user.getCreateTime().format(DTF));
        vo.setUpdateTime(user.getUpdateTime() == null ? null : user.getUpdateTime().format(DTF));
        return vo;
    }

    private List<MenuVO> buildMenuTree(List<MenuVO> nodes) {
        Map<String, MenuVO> nodeMap = new LinkedHashMap<>();
        for (MenuVO node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : nodes) {
            MenuVO parent = nodeMap.get(node.getParentId());
            if (parent == null || Objects.equals(node.getParentId(), String.valueOf(ROOT_PARENT_ID))) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        sortMenuTree(roots);
        return roots;
    }

    private void sortMenuTree(List<MenuVO> nodes) {
        nodes.sort(Comparator.comparing(MenuVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuVO::getId));
        for (MenuVO node : nodes) {
            sortMenuTree(node.getChildren());
        }
    }

    private MenuVO toMenuVo(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(String.valueOf(menu.getId()));
        vo.setMenuName(menu.getMenuName());
        vo.setParentId(String.valueOf(menu.getParentId()));
        vo.setSortOrder(menu.getSortOrder());
        vo.setRoutePath(menu.getRoutePath());
        vo.setComponentPath(menu.getComponentPath());
        vo.setMenuType(menu.getMenuType());
        vo.setPerms(menu.getPerms());
        vo.setIcon(menu.getIcon());
        vo.setVisible(menu.getVisible() == null ? null : menu.getVisible().intValue());
        vo.setStatus(menu.getStatus() == null ? null : menu.getStatus().intValue());
        return vo;
    }

    private long pageNum(RoleQueryDTO dto) {
        return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
    }

    private long pageSize(RoleQueryDTO dto) {
        return dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
    }

    private long pageNum(RoleUserQueryDTO dto) {
        return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
    }

    private long pageSize(RoleUserQueryDTO dto) {
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
