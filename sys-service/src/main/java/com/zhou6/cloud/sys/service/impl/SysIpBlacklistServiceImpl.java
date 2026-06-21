package com.zhou6.cloud.sys.service.impl;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.sys.dto.IpBlacklistDTO;
import com.zhou6.cloud.sys.dto.IpBlacklistQueryDTO;
import com.zhou6.cloud.sys.entity.SysIpBlacklist;
import com.zhou6.cloud.sys.mapper.SysIpBlacklistMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import com.zhou6.cloud.sys.service.SysIpBlacklistService;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.IpBlacklistVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysIpBlacklistServiceImpl extends BaseSysService implements SysIpBlacklistService {

    private final SysIpBlacklistMapper ipBlacklistMapper;
    private final SysCacheService cacheService;

    public SysIpBlacklistServiceImpl(SysIpBlacklistMapper ipBlacklistMapper, SysCacheService cacheService) {
        this.ipBlacklistMapper = ipBlacklistMapper;
        this.cacheService = cacheService;
    }

    @Override
    public PageResponse<IpBlacklistVO> page(IpBlacklistQueryDTO dto) {
        IpBlacklistQueryDTO query = dto == null ? new IpBlacklistQueryDTO() : dto;
        Page<SysIpBlacklist> page = ipBlacklistMapper.selectPage(
                Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<SysIpBlacklist>()
                        .like(hasText(query.getIpAddress()), SysIpBlacklist::getIpAddress, query.getIpAddress())
                        .eq(query.getIsStatus() != null, SysIpBlacklist::getIsStatus,
                                query.getIsStatus() == null ? null : query.getIsStatus().shortValue())
                        .orderByDesc(SysIpBlacklist::getCreateTime)
                        .orderByDesc(SysIpBlacklist::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toVo).toList());
    }

    @Override
    public void add(IpBlacklistDTO dto) {
        String ipAddress = requireValidAndNormalize(dto);
        require(!exists(ipAddress, null), "IP 黑名单已存在");
        SysIpBlacklist entity = fill(new SysIpBlacklist(), dto, ipAddress);
        ipBlacklistMapper.insert(entity);
        cacheService.refreshIpBlacklist(entity.getIpAddress(), entity.getIsStatus());
    }

    @Override
    public void edit(IpBlacklistDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "IP 黑名单ID不能为空");
        String ipAddress = requireValidAndNormalize(dto);
        SysIpBlacklist old = ipBlacklistMapper.selectById(id);
        require(old != null, "IP 黑名单不存在");
        require(!exists(ipAddress, id), "IP 黑名单已存在");
        SysIpBlacklist entity = fill(old, dto, ipAddress);
        ipBlacklistMapper.updateById(entity);
        if (!Objects.equals(old.getIpAddress(), entity.getIpAddress())) {
            cacheService.removeIpBlacklist(old.getIpAddress());
        }
        cacheService.refreshIpBlacklist(entity.getIpAddress(), entity.getIsStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        require(ids != null && !ids.isEmpty(), "IP 黑名单ID不能为空");
        List<Long> blacklistIds = ids.stream()
                .map(id -> parseRequiredId(id, "IP 黑名单ID不正确"))
                .distinct()
                .toList();
        List<SysIpBlacklist> blacklists = ipBlacklistMapper.selectBatchIds(blacklistIds);
        require(blacklists.size() == blacklistIds.size(), "IP 黑名单不存在");
        ipBlacklistMapper.deleteByIds(blacklistIds);
        for (SysIpBlacklist blacklist : blacklists) {
            cacheService.removeIpBlacklist(blacklist.getIpAddress());
        }
    }

    private String requireValidAndNormalize(IpBlacklistDTO dto) {
        require(dto != null && hasText(dto.getIpAddress()), "IP 地址不能为空");
        String ipAddress = dto.getIpAddress().trim();
        require(ipAddress.matches("[0-9a-fA-F:.]+"), "IP 地址格式不正确");
        try {
            return InetAddress.getByName(ipAddress).getHostAddress();
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "IP 地址格式不正确");
        }
    }

    private SysIpBlacklist fill(SysIpBlacklist entity, IpBlacklistDTO dto, String ipAddress) {
        entity.setIpAddress(ipAddress);
        entity.setIsStatus(toStatus(dto.getIsStatus()));
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private IpBlacklistVO toVo(SysIpBlacklist entity) {
        IpBlacklistVO vo = new IpBlacklistVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setIpAddress(entity.getIpAddress());
        vo.setIsStatus(entity.getIsStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private boolean exists(String ipAddress, Long selfId) {
        SysIpBlacklist existed = ipBlacklistMapper.selectOne(new LambdaQueryWrapper<SysIpBlacklist>()
                .eq(SysIpBlacklist::getIpAddress, ipAddress)
                .last("limit 1"));
        return existed != null && !Objects.equals(existed.getId(), selfId);
    }
}
