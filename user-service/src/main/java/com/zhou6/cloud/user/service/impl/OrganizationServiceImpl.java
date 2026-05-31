package com.zhou6.cloud.user.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.OrgAddDTO;
import com.zhou6.cloud.user.dto.OrgChangeStatusDTO;
import com.zhou6.cloud.user.dto.OrgChildrenQueryDTO;
import com.zhou6.cloud.user.vo.OrgDetailVO;
import com.zhou6.cloud.user.dto.OrgEditDTO;
import com.zhou6.cloud.user.dto.OrgIdDTO;
import com.zhou6.cloud.user.dto.OrgTreeQueryDTO;
import com.zhou6.cloud.user.vo.OrgTreeVO;
import com.zhou6.cloud.user.dto.OrgUserAddDTO;
import com.zhou6.cloud.user.dto.OrgUserPageDTO;
import com.zhou6.cloud.user.dto.OrgUserRemoveDTO;
import com.zhou6.cloud.user.dto.OrgUserSetPrimaryDTO;
import com.zhou6.cloud.user.vo.OrgUserVO;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.entity.SysOrganization;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.entity.SysUserOrganization;
import com.zhou6.cloud.user.mapper.SysOrganizationMapper;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.mapper.SysUserOrganizationMapper;
import com.zhou6.cloud.user.service.OrganizationService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织部门业务实现，负责维护部门树结构和部门人员关系。
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final short DELETED_NO = 0;
    private static final short DELETED_YES = 1;
    private static final java.time.format.DateTimeFormatter DTF = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysOrganizationMapper organizationMapper;
    private final SysUserOrganizationMapper userOrganizationMapper;
    private final SysUserMapper userMapper;

    public OrganizationServiceImpl(SysOrganizationMapper organizationMapper,
            SysUserOrganizationMapper userOrganizationMapper, SysUserMapper userMapper) {
        this.organizationMapper = organizationMapper;
        this.userOrganizationMapper = userOrganizationMapper;
        this.userMapper = userMapper;
    }

    /**
     * 新增部门，并在插入后回写完整树路径。
     *
     * @param dto 新增部门参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrganization(OrgAddDTO dto) {
        require(dto != null && hasText(dto.getOrgName()), "部门名称不能为空");
        requireValidOrgType(dto.getOrgType());
        require(dto.getStatus() == null || StatusConstants.isValidStatus(dto.getStatus()), "机构状态不正确");
        SysOrganization parent = findParent(normalizeParentId(dto.getParentId()));
        String orgCode = normalizeBlank(dto.getOrgCode());
        requireOrgCodeUnique(orgCode, null);
        requireLeaderExists(dto.getLeaderId());
        SysOrganization organization = new SysOrganization();
        organization.setParentId(parentId(parent));
        organization.setOrgName(dto.getOrgName());
        organization.setOrgType(dto.getOrgType());
        organization.setOrgCode(orgCode);
        organization.setLeaderId(dto.getLeaderId());
        organization.setStatus(dto.getStatus() == null ? StatusConstants.STATUS_ENABLED : dto.getStatus());
        organization.setTreeLevel(parent == null ? 1 : parent.getTreeLevel() + 1);
        organization.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        organization.setIsDeleted(DELETED_NO);
        organizationMapper.insert(organization);

        organization.setTreePath(buildTreePath(parent, organization.getId()));
        organizationMapper.updateById(organization);
    }

    /**
     * 修改部门基础信息；当父级变化时同步刷新子部门树路径。
     *
     * @param dto 修改部门参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editOrganization(OrgEditDTO dto) {
        require(dto != null && dto.getId() != null, "部门ID不能为空");
        require(hasText(dto.getOrgName()), "部门名称不能为空");
        requireValidOrgType(dto.getOrgType());
        require(dto.getStatus() == null || StatusConstants.isValidStatus(dto.getStatus()), "机构状态不正确");
        SysOrganization organization = getRequiredOrganization(dto.getId());
        SysOrganization parent = findParent(normalizeParentId(dto.getParentId()));
        if (parent != null) {
            require(!Objects.equals(parent.getId(), organization.getId()), "上级部门不能是自己");
            require(!isSelfOrDescendantPath(parent.getTreePath(), organization.getTreePath()),
                    "上级部门不能选择自己的子部门");
        }

        String orgCode = normalizeBlank(dto.getOrgCode());
        requireOrgCodeUnique(orgCode, organization.getId());
        requireLeaderExists(dto.getLeaderId());
        String oldTreePath = organization.getTreePath();
        String newTreePath = buildTreePath(parent, organization.getId());
        organization.setParentId(parentId(parent));
        organization.setOrgName(dto.getOrgName());
        organization.setOrgType(dto.getOrgType());
        organization.setOrgCode(orgCode);
        organization.setLeaderId(dto.getLeaderId());
        organization.setStatus(dto.getStatus() == null ? organization.getStatus() : dto.getStatus());
        organization.setTreeLevel(parent == null ? 1 : parent.getTreeLevel() + 1);
        organization.setTreePath(newTreePath);
        organization.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        organizationMapper.updateById(organization);
        refreshChildrenPath(oldTreePath, newTreePath, organization.getTreeLevel());
    }

    /**
     * 删除部门及其所有子部门，同时清理部门人员关系。
     *
     * @param dto 部门 ID 参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrganization(OrgIdDTO dto) {
        SysOrganization organization = getRequiredOrganization(requireId(dto));
        List<SysOrganization> targets = organizationMapper.selectList(activeOrgWrapper()
                .and(wrapper -> wrapper.eq(SysOrganization::getTreePath, organization.getTreePath())
                        .or()
                        .likeRight(SysOrganization::getTreePath, organization.getTreePath() + ",")));
        List<Long> orgIds = targets.stream().map(SysOrganization::getId).toList();
        if (!orgIds.isEmpty()) {
            organizationMapper.update(null, new LambdaUpdateWrapper<SysOrganization>()
                    .in(SysOrganization::getId, orgIds)
                    .set(SysOrganization::getIsDeleted, DELETED_YES));
            userOrganizationMapper.delete(new LambdaQueryWrapper<SysUserOrganization>()
                    .in(SysUserOrganization::getOrgId, orgIds));
        }
    }

    /**
     * 修改部门状态。
     *
     * @param dto 状态修改参数
     */
    @Override
    public void changeStatus(OrgChangeStatusDTO dto) {
        require(dto != null && dto.getId() != null, "部门ID不能为空");
        require(dto.getStatus() != null, "部门状态不能为空");
        require(StatusConstants.isValidStatus(dto.getStatus()), "部门状态不正确");
        organizationMapper.update(null, new LambdaUpdateWrapper<SysOrganization>()
                .eq(SysOrganization::getId, dto.getId())
                .eq(SysOrganization::getIsDeleted, DELETED_NO)
                .set(SysOrganization::getStatus, dto.getStatus()));
    }

    /**
     * 按 ID 查询部门详情。
     *
     * @param dto 部门 ID 参数
     * @return 部门详情
     */
    @Override
    public OrgDetailVO getById(OrgIdDTO dto) {
        return toDetail(getRequiredOrganization(requireId(dto)));
    }

    /**
     * 查询部门树，可按状态过滤。
     *
     * @param dto 树查询过滤参数
     * @return 部门树
     */
    @Override
    public List<OrgTreeVO> getTree(OrgTreeQueryDTO dto) {
        LambdaQueryWrapper<SysOrganization> wrapper = orderedOrgWrapper();
        applyOrgQuery(wrapper, dto);
        List<SysOrganization> organizations = organizationMapper.selectList(wrapper);
        Map<Long, String> leaderNames = leaderNameMap(organizations);
        List<OrgTreeVO> nodes = organizations.stream()
                .map(organization -> toTree(organization, leaderNames))
                .toList();
        return buildTree(nodes);
    }

    /**
     * 导出组织机构表扁平列表。
     *
     * @param dto 查询过滤参数
     * @param response 文件响应
     */
    @Override
    public void export(OrgTreeQueryDTO dto, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<SysOrganization> wrapper = orderedOrgWrapper();
        applyOrgQuery(wrapper, dto);
        List<SysOrganization> organizations = organizationMapper.selectList(wrapper);
        Map<Long, String> leaderNames = leaderNameMap(organizations);
        List<OrgDetailVO> records = organizations.stream()
                .map(organization -> toDetail(organization, leaderNames))
                .toList();
        writeOrganizationExcel(records, response);
    }

    /**
     * 查询指定父部门下的直接子部门。
     *
     * @param dto 父部门和过滤参数
     * @return 子部门列表
     */
    @Override
    public List<OrgDetailVO> getChildren(OrgChildrenQueryDTO dto) {
        Long parentId = dto == null ? ROOT_PARENT_ID : normalizeParentId(dto.getParentId());
        LambdaQueryWrapper<SysOrganization> wrapper = orderedOrgWrapper()
                .eq(SysOrganization::getParentId, parentId);
        applyOrgQuery(wrapper, dto);
        List<SysOrganization> organizations = organizationMapper.selectList(wrapper);
        Map<Long, String> leaderNames = leaderNameMap(organizations);
        return organizations.stream().map(organization -> toDetail(organization, leaderNames)).toList();
    }

    /**
     * 查询指定部门下所有层级子部门。
     *
     * @param dto 部门 ID 参数
     * @return 子孙部门列表
     */
    @Override
    public List<OrgDetailVO> getDescendants(OrgIdDTO dto) {
        SysOrganization organization = getRequiredOrganization(requireId(dto));
        List<SysOrganization> organizations = organizationMapper.selectList(orderedOrgWrapper()
                        .and(wrapper -> wrapper.eq(SysOrganization::getTreePath, organization.getTreePath())
                                .or()
                                .likeRight(SysOrganization::getTreePath, organization.getTreePath() + ","))
                        .ne(SysOrganization::getId, organization.getId()));
        Map<Long, String> leaderNames = leaderNameMap(organizations);
        return organizations.stream().map(item -> toDetail(item, leaderNames)).toList();
    }

    /**
     * 分页查询部门人员，并合并用户基础信息。
     *
     * @param dto 分页查询参数
     * @return 部门人员分页结果
     */
    @Override
    public PageResponse<OrgUserVO> pageUsers(OrgUserPageDTO dto) {
        require(dto != null && dto.getOrgId() != null, "部门ID不能为空");
        long pageNum = dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        Page<SysUserOrganization> page = userOrganizationMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<SysUserOrganization>()
                        .eq(SysUserOrganization::getOrgId, dto.getOrgId())
                        .orderByDesc(SysUserOrganization::getIsPrimary)
                        .orderByAsc(SysUserOrganization::getUserId));
        if (page.getRecords().isEmpty()) {
            return new PageResponse<>(page.getTotal(), pageNum, pageSize, Collections.emptyList());
        }
        List<Long> userIds = page.getRecords().stream().map(SysUserOrganization::getUserId).toList();
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        List<OrgUserVO> records = page.getRecords().stream()
                .map(relation -> toOrgUser(relation, userMap.get(relation.getUserId())))
                .filter(Objects::nonNull)
                .toList();
        return new PageResponse<>(page.getTotal(), pageNum, pageSize, records);
    }

    /**
     * 批量向部门添加人员。
     *
     * @param dto 部门人员新增参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUsers(OrgUserAddDTO dto) {
        require(dto != null && dto.getOrgId() != null, "部门ID不能为空");
        require(dto.getUserIds() != null && !dto.getUserIds().isEmpty(), "用户ID不能为空");
        getRequiredOrganization(dto.getOrgId());
        short isPrimary = Objects.equals(dto.getIsPrimary(), StatusConstants.PRIMARY_YES) ? StatusConstants.PRIMARY_YES : StatusConstants.PRIMARY_NO;
        for (Long userId : dto.getUserIds()) {
            if (userId == null) {
                continue;
            }
            if (isPrimary == StatusConstants.PRIMARY_YES) {
                clearPrimary(userId);
            }
            upsertRelation(dto.getOrgId(), userId, isPrimary);
        }
    }

    /**
     * 从部门中移除指定人员。
     *
     * @param dto 部门人员移除参数
     */
    @Override
    public void removeUser(OrgUserRemoveDTO dto) {
        require(dto != null && dto.getOrgId() != null && dto.getUserId() != null, "部门ID和用户ID不能为空");
        userOrganizationMapper.delete(new LambdaQueryWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getOrgId, dto.getOrgId())
                .eq(SysUserOrganization::getUserId, dto.getUserId()));
    }

    /**
     * 将指定部门设置为用户主部门。
     *
     * @param dto 主部门设置参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPrimary(OrgUserSetPrimaryDTO dto) {
        require(dto != null && dto.getOrgId() != null && dto.getUserId() != null, "部门ID和用户ID不能为空");
        getRequiredOrganization(dto.getOrgId());
        clearPrimary(dto.getUserId());
        upsertRelation(dto.getOrgId(), dto.getUserId(), StatusConstants.PRIMARY_YES);
    }

    private void upsertRelation(Long orgId, Long userId, short isPrimary) {
        SysUserOrganization relation = userOrganizationMapper.selectOne(new LambdaQueryWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getOrgId, orgId)
                .eq(SysUserOrganization::getUserId, userId)
                .last("limit 1"));
        if (relation == null) {
            relation = new SysUserOrganization();
            relation.setOrgId(orgId);
            relation.setUserId(userId);
            relation.setIsPrimary(isPrimary);
            userOrganizationMapper.insert(relation);
            return;
        }
        userOrganizationMapper.update(null, new LambdaUpdateWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getOrgId, orgId)
                .eq(SysUserOrganization::getUserId, userId)
                .set(SysUserOrganization::getIsPrimary, isPrimary));
    }

    private void clearPrimary(Long userId) {
        userOrganizationMapper.update(null, new LambdaUpdateWrapper<SysUserOrganization>()
                .eq(SysUserOrganization::getUserId, userId)
                .set(SysUserOrganization::getIsPrimary, StatusConstants.PRIMARY_NO));
    }

    private void refreshChildrenPath(String oldTreePath, String newTreePath, Integer parentLevel) {
        if (oldTreePath == null || newTreePath == null) {
            return;
        }
        List<SysOrganization> children = organizationMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getIsDeleted, DELETED_NO)
                .likeRight(SysOrganization::getTreePath, oldTreePath + ",")
                .ne(SysOrganization::getTreePath, oldTreePath));
        for (SysOrganization child : children) {
            String childPath = child.getTreePath().replaceFirst("^" + java.util.regex.Pattern.quote(oldTreePath), newTreePath);
            child.setTreePath(childPath);
            child.setTreeLevel(parentLevel + countDescendantLevel(newTreePath, childPath));
            organizationMapper.updateById(child);
        }
    }

    private int countDescendantLevel(String parentPath, String childPath) {
        String suffix = childPath.substring(parentPath.length());
        if (suffix.isBlank()) {
            return 0;
        }
        int level = 0;
        for (String item : suffix.split(",")) {
            if (hasText(item)) {
                level++;
            }
        }
        return level;
    }

    private List<OrgTreeVO> buildTree(List<OrgTreeVO> nodes) {
        Map<String, OrgTreeVO> nodeMap = new LinkedHashMap<>();
        for (OrgTreeVO node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        List<OrgTreeVO> roots = new ArrayList<>();
        for (OrgTreeVO node : nodes) {
            OrgTreeVO parent = nodeMap.get(node.getParentId());
            if (parent == null || Objects.equals(node.getParentId(), String.valueOf(ROOT_PARENT_ID))) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<OrgTreeVO> nodes) {
        nodes.sort(Comparator.comparing(OrgTreeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(OrgTreeVO::getId));
        for (OrgTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private LambdaQueryWrapper<SysOrganization> orderedOrgWrapper() {
        return activeOrgWrapper()
                .orderByAsc(SysOrganization::getSortOrder)
                .orderByAsc(SysOrganization::getId);
    }

    private void applyOrgQuery(LambdaQueryWrapper<SysOrganization> wrapper, OrgTreeQueryDTO dto) {
        if (dto == null) {
            return;
        }
        wrapper.like(hasText(dto.getOrgName()), SysOrganization::getOrgName, normalizeBlank(dto.getOrgName()))
                .eq(dto.getOrgType() != null, SysOrganization::getOrgType, dto.getOrgType())
                .eq(dto.getStatus() != null, SysOrganization::getStatus, dto.getStatus());
    }

    private void applyOrgQuery(LambdaQueryWrapper<SysOrganization> wrapper, OrgChildrenQueryDTO dto) {
        if (dto == null) {
            return;
        }
        wrapper.like(hasText(dto.getOrgName()), SysOrganization::getOrgName, normalizeBlank(dto.getOrgName()))
                .eq(dto.getOrgType() != null, SysOrganization::getOrgType, dto.getOrgType())
                .eq(dto.getStatus() != null, SysOrganization::getStatus, dto.getStatus());
    }

    private LambdaQueryWrapper<SysOrganization> activeOrgWrapper() {
        return new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getIsDeleted, DELETED_NO);
    }

    private SysOrganization findParent(Long parentId) {
        if (parentId == null || Objects.equals(parentId, ROOT_PARENT_ID)) {
            return null;
        }
        return getRequiredOrganization(parentId);
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private Long parentId(SysOrganization parent) {
        return parent == null ? ROOT_PARENT_ID : parent.getId();
    }

    private String buildTreePath(SysOrganization parent, Long id) {
        return parent == null ? ROOT_PARENT_ID + "," + id : parent.getTreePath() + "," + id;
    }

    private SysOrganization getRequiredOrganization(Long id) {
        SysOrganization organization = organizationMapper.selectOne(activeOrgWrapper()
                .eq(SysOrganization::getId, id)
                .last("limit 1"));
        require(organization != null, "部门不存在");
        return organization;
    }

    private void requireOrgCodeUnique(String orgCode, Long excludeId) {
        if (!hasText(orgCode)) {
            return;
        }
        LambdaQueryWrapper<SysOrganization> wrapper = new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getOrgCode, orgCode);
        if (excludeId != null) {
            wrapper.ne(SysOrganization::getId, excludeId);
        }
        Long count = organizationMapper.selectCount(wrapper);
        require(count == 0, "机构编码已存在");
    }

    private void requireValidOrgType(Short orgType) {
        require(orgType != null, "机构类型不能为空");
        require(orgType == 1 || orgType == 2 || orgType == 3, "机构类型不正确");
    }

    private void requireLeaderExists(Long leaderId) {
        if (leaderId == null) {
            return;
        }
        require(userMapper.selectById(leaderId) != null, "负责人用户不存在");
    }

    private boolean isSelfOrDescendantPath(String candidatePath, String currentPath) {
        return hasText(candidatePath) && hasText(currentPath)
                && (candidatePath.equals(currentPath) || candidatePath.startsWith(currentPath + ","));
    }

    private Long requireId(OrgIdDTO dto) {
        require(dto != null && dto.getId() != null, "部门ID不能为空");
        return dto.getId();
    }

    private void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeBlank(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private OrgDetailVO toDetail(SysOrganization organization) {
        return toDetail(organization, leaderNameMap(List.of(organization)));
    }

    private OrgDetailVO toDetail(SysOrganization organization, Map<Long, String> leaderNames) {
        OrgDetailVO vo = new OrgDetailVO();
        vo.setId(String.valueOf(organization.getId()));
        vo.setParentId(String.valueOf(organization.getParentId()));
        vo.setOrgName(organization.getOrgName());
        vo.setOrgType(organization.getOrgType());
        vo.setOrgCode(organization.getOrgCode());
        vo.setLeaderId(organization.getLeaderId() == null ? null : String.valueOf(organization.getLeaderId()));
        vo.setLeaderName(organization.getLeaderId() == null ? null : leaderNames.get(organization.getLeaderId()));
        vo.setStatus(organization.getStatus());
        vo.setTreePath(organization.getTreePath());
        vo.setTreeLevel(organization.getTreeLevel());
        vo.setSortOrder(organization.getSortOrder());
        vo.setIsDeleted(organization.getIsDeleted());
        vo.setCreateTime(formatTime(organization.getCreateTime()));
        vo.setUpdateTime(formatTime(organization.getUpdateTime()));
        return vo;
    }

    private OrgTreeVO toTree(SysOrganization organization) {
        return toTree(organization, leaderNameMap(List.of(organization)));
    }

    private OrgTreeVO toTree(SysOrganization organization, Map<Long, String> leaderNames) {
        OrgTreeVO vo = new OrgTreeVO();
        vo.setId(String.valueOf(organization.getId()));
        vo.setParentId(String.valueOf(organization.getParentId()));
        vo.setOrgName(organization.getOrgName());
        vo.setOrgType(organization.getOrgType());
        vo.setOrgCode(organization.getOrgCode());
        vo.setLeaderId(organization.getLeaderId() == null ? null : String.valueOf(organization.getLeaderId()));
        vo.setLeaderName(organization.getLeaderId() == null ? null : leaderNames.get(organization.getLeaderId()));
        vo.setStatus(organization.getStatus());
        vo.setSortOrder(organization.getSortOrder());
        vo.setCreateTime(formatTime(organization.getCreateTime()));
        vo.setUpdateTime(formatTime(organization.getUpdateTime()));
        return vo;
    }

    private Map<Long, String> leaderNameMap(List<SysOrganization> organizations) {
        List<Long> leaderIds = organizations.stream()
                .map(SysOrganization::getLeaderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (leaderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> names = new LinkedHashMap<>();
        for (SysUser user : userMapper.selectBatchIds(leaderIds)) {
            names.put(user.getId(), userDisplayName(user));
        }
        return names;
    }

    private String userDisplayName(SysUser user) {
        return hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private String formatTime(java.time.LocalDateTime time) {
        return time == null ? null : time.format(DTF);
    }

    private void writeOrganizationExcel(List<OrgDetailVO> records, HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("组织机构列表.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("组织机构列表");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "机构ID", "父级机构ID", "机构名称", "机构类型", "机构编码", "负责人ID", "负责人名称",
                    "状态", "树路径", "树层级", "显示顺序", "逻辑删除", "创建时间", "修改时间"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < records.size(); i++) {
                OrgDetailVO item = records.get(i);
                Row row = sheet.createRow(i + 1);
                writeRow(row,
                        item.getId(),
                        item.getParentId(),
                        item.getOrgName(),
                        orgTypeText(item.getOrgType()),
                        item.getOrgCode(),
                        item.getLeaderId(),
                        item.getLeaderName(),
                        statusText(item.getStatus()),
                        item.getTreePath(),
                        item.getTreeLevel() == null ? null : String.valueOf(item.getTreeLevel()),
                        item.getSortOrder() == null ? null : String.valueOf(item.getSortOrder()),
                        deletedText(item.getIsDeleted()),
                        item.getCreateTime(),
                        item.getUpdateTime());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private void writeRow(Row row, String... values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i] == null ? "" : values[i]);
        }
    }

    private String orgTypeText(Short orgType) {
        if (orgType == null) {
            return "";
        }
        return switch (orgType) {
            case 1 -> "单位/公司";
            case 2 -> "部门";
            case 3 -> "班组";
            default -> String.valueOf(orgType);
        };
    }

    private String statusText(Short status) {
        if (status == null) {
            return "";
        }
        return Objects.equals(status, StatusConstants.STATUS_ENABLED) ? "正常" : "停用";
    }

    private String deletedText(Short isDeleted) {
        if (isDeleted == null) {
            return "";
        }
        return Objects.equals(isDeleted, DELETED_YES) ? "已删除" : "未删除";
    }

    private OrgUserVO toOrgUser(SysUserOrganization relation, SysUser user) {
        if (user == null) {
            return null;
        }
        OrgUserVO vo = new OrgUserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setContactPhone(user.getContactPhone());
        vo.setIsPrimary(relation.getIsPrimary());
        return vo;
    }
}
