package com.zhou6.cloud.sys.service.impl;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.sys.dto.WhitelistDTO;
import com.zhou6.cloud.sys.dto.WhitelistQueryDTO;
import com.zhou6.cloud.sys.entity.SysWhitelist;
import com.zhou6.cloud.sys.mapper.SysWhitelistMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import com.zhou6.cloud.sys.service.SysWhitelistService;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.WhitelistVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysWhitelistServiceImpl extends BaseSysService implements SysWhitelistService {

    private final SysWhitelistMapper whitelistMapper;
    private final SysCacheService cacheService;

    public SysWhitelistServiceImpl(SysWhitelistMapper whitelistMapper, SysCacheService cacheService) {
        this.whitelistMapper = whitelistMapper;
        this.cacheService = cacheService;
    }

    @Override
    public PageResponse<WhitelistVO> page(WhitelistQueryDTO dto) {
        WhitelistQueryDTO query = dto == null ? new WhitelistQueryDTO() : dto;
        Page<SysWhitelist> page = whitelistMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<SysWhitelist>()
                        .eq(hasText(query.getType()), SysWhitelist::getType, query.getType())
                        .like(hasText(query.getValue()), SysWhitelist::getValue, query.getValue())
                        .eq(query.getIsStatus() != null, SysWhitelist::getIsStatus,
                                query.getIsStatus() == null ? null : query.getIsStatus().shortValue())
                        .orderByDesc(SysWhitelist::getCreateTime)
                        .orderByDesc(SysWhitelist::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toVo).toList());
    }

    @Override
    public void add(WhitelistDTO dto) {
        requireValid(dto);
        require(!exists(dto.getType(), dto.getValue(), null), "白名单已存在");
        SysWhitelist entity = fill(new SysWhitelist(), dto);
        whitelistMapper.insert(entity);
        cacheService.refreshWhitelist(entity.getType(), entity.getValue(), entity.getIsStatus());
    }

    @Override
    public void edit(WhitelistDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "白名单ID不能为空");
        requireValid(dto);
        SysWhitelist old = whitelistMapper.selectById(id);
        require(old != null, "白名单不存在");
        require(!exists(dto.getType(), dto.getValue(), id), "白名单已存在");
        SysWhitelist entity = fill(old, dto);
        whitelistMapper.updateById(entity);
        if (!Objects.equals(old.getType(), entity.getType()) || !Objects.equals(old.getValue(), entity.getValue())) {
            cacheService.removeWhitelist(old.getType(), old.getValue());
        }
        cacheService.refreshWhitelist(entity.getType(), entity.getValue(), entity.getIsStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        require(ids != null && !ids.isEmpty(), "白名单ID不能为空");
        List<Long> whitelistIds = ids.stream()
                .map(id -> parseRequiredId(id, "白名单ID不正确"))
                .distinct()
                .toList();
        List<SysWhitelist> whitelists = whitelistMapper.selectBatchIds(whitelistIds);
        require(whitelists.size() == whitelistIds.size(), "白名单不存在");
        whitelistMapper.deleteByIds(whitelistIds);
        for (SysWhitelist whitelist : whitelists) {
            cacheService.removeWhitelist(whitelist.getType(), whitelist.getValue());
        }
    }

    private void requireValid(WhitelistDTO dto) {
        require(dto != null, "白名单参数不能为空");
        require(hasText(dto.getType()), "白名单类型不能为空");
        require(hasText(dto.getValue()), "白名单值不能为空");
    }

    private SysWhitelist fill(SysWhitelist entity, WhitelistDTO dto) {
        entity.setType(dto.getType());
        entity.setValue(dto.getValue());
        entity.setIsStatus(toStatus(dto.getIsStatus()));
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private WhitelistVO toVo(SysWhitelist entity) {
        WhitelistVO vo = new WhitelistVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setType(entity.getType());
        vo.setValue(entity.getValue());
        vo.setIsStatus(entity.getIsStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private boolean exists(String type, String value, Long selfId) {
        SysWhitelist existed = whitelistMapper.selectOne(new LambdaQueryWrapper<SysWhitelist>()
                .eq(SysWhitelist::getType, type)
                .eq(SysWhitelist::getValue, value)
                .last("limit 1"));
        return existed != null && !Objects.equals(existed.getId(), selfId);
    }
}
