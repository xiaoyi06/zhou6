package com.zhou6.cloud.user.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.ExternalSystemChangeStatusDTO;
import com.zhou6.cloud.user.dto.ExternalSystemIdDTO;
import com.zhou6.cloud.user.dto.ExternalSystemQueryDTO;
import com.zhou6.cloud.user.dto.ExternalSystemSaveDTO;
import com.zhou6.cloud.user.entity.SysExternalSystem;
import com.zhou6.cloud.user.entity.SysRole;
import com.zhou6.cloud.user.mapper.SysExternalSystemMapper;
import com.zhou6.cloud.user.mapper.SysRoleMapper;
import com.zhou6.cloud.user.service.ExternalSystemService;
import com.zhou6.cloud.user.vo.ExternalSystemVO;
import com.zhou6.cloud.user.vo.PageResponse;
import org.springframework.stereotype.Service;

/** 外部系统管理业务实现。 */
@Service
public class ExternalSystemServiceImpl implements ExternalSystemService {

    private final SysExternalSystemMapper externalSystemMapper;
    private final SysRoleMapper roleMapper;

    public ExternalSystemServiceImpl(SysExternalSystemMapper externalSystemMapper, SysRoleMapper roleMapper) {
        this.externalSystemMapper = externalSystemMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public PageResponse<ExternalSystemVO> page(ExternalSystemQueryDTO dto) {
        ExternalSystemQueryDTO query = dto == null ? new ExternalSystemQueryDTO() : dto;
        Page<SysExternalSystem> page = externalSystemMapper.selectPage(Page.of(pageNum(query), pageSize(query)),
                new LambdaQueryWrapper<SysExternalSystem>()
                        .like(hasText(query.getSystemName()), SysExternalSystem::getSystemName, query.getSystemName())
                        .like(hasText(query.getSystemCode()), SysExternalSystem::getSystemCode, query.getSystemCode())
                        .eq(query.getStatus() != null, SysExternalSystem::getStatus,
                                query.getStatus() == null ? null : query.getStatus().shortValue())
                        .orderByAsc(SysExternalSystem::getSortOrder)
                        .orderByDesc(SysExternalSystem::getCreateTime)
                        .orderByDesc(SysExternalSystem::getId));
        return new PageResponse<>(page.getTotal(), pageNum(query), pageSize(query),
                page.getRecords().stream().map(this::toVo).toList());
    }

    @Override
    public void add(ExternalSystemSaveDTO dto) {
        require(dto != null, "外部系统参数不能为空");
        require(hasText(dto.getSystemName()), "系统名称不能为空");
        require(hasText(dto.getSystemCode()), "系统编码不能为空");
        checkSystemCodeUnique(dto.getSystemCode());
        SysExternalSystem system = new SysExternalSystem();
        system.setSystemName(dto.getSystemName());
        system.setSystemCode(dto.getSystemCode());
        system.setSystemUrl(dto.getSystemUrl());
        system.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        system.setStatus(StatusConstants.STATUS_ENABLED);
        system.setRemark(dto.getRemark());
        externalSystemMapper.insert(system);
    }

    @Override
    public void edit(ExternalSystemSaveDTO dto) {
        require(dto != null && hasText(dto.getId()), "系统ID不能为空");
        require(hasText(dto.getSystemName()), "系统名称不能为空");
        Long systemId = parseRequiredId(dto.getId(), "系统ID不正确");
        getRequiredSystem(systemId);
        externalSystemMapper.update(null, new LambdaUpdateWrapper<SysExternalSystem>()
                .eq(SysExternalSystem::getId, systemId)
                .set(SysExternalSystem::getSystemName, dto.getSystemName())
                .set(SysExternalSystem::getSystemUrl, dto.getSystemUrl())
                .set(SysExternalSystem::getSortOrder, dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                .set(SysExternalSystem::getRemark, dto.getRemark()));
    }

    @Override
    public void delete(ExternalSystemIdDTO dto) {
        Long systemId = parseRequiredId(dto == null ? null : dto.getId(), "系统ID不能为空");
        getRequiredSystem(systemId);
        long roleCount = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getSystemId, systemId));
        require(roleCount == 0, "该系统下存在角色，无法删除");
        externalSystemMapper.deleteById(systemId);
    }

    @Override
    public void changeStatus(ExternalSystemChangeStatusDTO dto) {
        require(dto != null && hasText(dto.getId()), "系统ID不能为空");
        require(dto.getStatus() != null, "系统状态不能为空");
        Long systemId = parseRequiredId(dto.getId(), "系统ID不正确");
        getRequiredSystem(systemId);
        externalSystemMapper.update(null, new LambdaUpdateWrapper<SysExternalSystem>()
                .eq(SysExternalSystem::getId, systemId)
                .set(SysExternalSystem::getStatus, dto.getStatus().shortValue()));
    }

    @Override
    public List<ExternalSystemVO> listEnabled() {
        return externalSystemMapper.selectList(new LambdaQueryWrapper<SysExternalSystem>()
                        .eq(SysExternalSystem::getStatus, StatusConstants.STATUS_ENABLED)
                        .orderByAsc(SysExternalSystem::getSortOrder)
                        .orderByDesc(SysExternalSystem::getCreateTime)
                        .orderByDesc(SysExternalSystem::getId))
                .stream().map(this::toVo).toList();
    }

    private void checkSystemCodeUnique(String systemCode) {
        SysExternalSystem system = externalSystemMapper.selectOne(new LambdaQueryWrapper<SysExternalSystem>()
                .eq(SysExternalSystem::getSystemCode, systemCode).last("limit 1"));
        require(system == null, "系统编码已存在");
    }

    private SysExternalSystem getRequiredSystem(Long systemId) {
        SysExternalSystem system = externalSystemMapper.selectById(systemId);
        require(system != null, "外部系统不存在");
        return system;
    }

    private ExternalSystemVO toVo(SysExternalSystem system) {
        ExternalSystemVO vo = new ExternalSystemVO();
        vo.setId(String.valueOf(system.getId()));
        vo.setSystemName(system.getSystemName());
        vo.setSystemCode(system.getSystemCode());
        vo.setSystemUrl(system.getSystemUrl());
        vo.setSortOrder(system.getSortOrder());
        vo.setStatus(system.getStatus() == null ? null : system.getStatus().intValue());
        vo.setRemark(system.getRemark());
        return vo;
    }

    private long pageNum(ExternalSystemQueryDTO dto) { return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum(); }
    private long pageSize(ExternalSystemQueryDTO dto) { return dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize(); }
    private Long parseRequiredId(String value, String message) {
        require(hasText(value), message);
        try { return Long.valueOf(value); } catch (NumberFormatException ex) { throw new BizException(CommonErrorCode.PARAM_INVALID, message); }
    }
    private void require(boolean expression, String message) { if (!expression) { throw new BizException(CommonErrorCode.PARAM_INVALID, message); } }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
