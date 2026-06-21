package com.zhou6.cloud.sys.service.impl;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.sys.dto.DictDataDTO;
import com.zhou6.cloud.sys.dto.DictQueryDTO;
import com.zhou6.cloud.sys.dto.DictTypeDTO;
import com.zhou6.cloud.sys.entity.SysDictData;
import com.zhou6.cloud.sys.entity.SysDictType;
import com.zhou6.cloud.sys.mapper.SysDictDataMapper;
import com.zhou6.cloud.sys.mapper.SysDictTypeMapper;
import com.zhou6.cloud.sys.service.SysCacheService;
import com.zhou6.cloud.sys.service.SysDictService;
import com.zhou6.cloud.sys.vo.DictDataVO;
import com.zhou6.cloud.sys.vo.DictTypeVO;
import com.zhou6.cloud.sys.vo.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class SysDictServiceImpl extends BaseSysService implements SysDictService {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;
    private final SysCacheService cacheService;

    public SysDictServiceImpl(SysDictTypeMapper typeMapper, SysDictDataMapper dataMapper, SysCacheService cacheService) {
        this.typeMapper = typeMapper;
        this.dataMapper = dataMapper;
        this.cacheService = cacheService;
    }

    @Override
    public PageResponse<DictTypeVO> typePage(DictQueryDTO dto) {
        DictQueryDTO query = dto == null ? new DictQueryDTO() : dto;
        Page<SysDictType> page = typeMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<SysDictType>()
                        .like(hasText(query.getDictName()), SysDictType::getDictName, query.getDictName())
                        .like(hasText(query.getDictType()), SysDictType::getDictType, query.getDictType())
                        .eq(query.getIsStatus() != null, SysDictType::getIsStatus,
                                query.getIsStatus() == null ? null : query.getIsStatus().shortValue())
                        .orderByDesc(SysDictType::getUpdateTime)
                        .orderByDesc(SysDictType::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toTypeVO).toList());
    }

    @Override
    public List<DictDataVO> dataList(DictQueryDTO dto) {
        DictQueryDTO query = dto == null ? new DictQueryDTO() : dto;
        require(hasText(query.getDictType()), "字典类型不能为空");
        return dataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, query.getDictType())
                .eq(query.getIsStatus() != null, SysDictData::getIsStatus,
                        query.getIsStatus() == null ? null : query.getIsStatus().shortValue())
                .orderByAsc(SysDictData::getDictSort)
                .orderByAsc(SysDictData::getId)).stream().map(this::toDataVO).toList();
    }

    @Override
    public PageResponse<DictDataVO> dataPage(DictQueryDTO dto) {
        DictQueryDTO query = dto == null ? new DictQueryDTO() : dto;
        require(hasText(query.getDictType()), "字典类型不能为空");
        Page<SysDictData> page = dataMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, query.getDictType())
                        .eq(query.getIsStatus() != null, SysDictData::getIsStatus,
                                query.getIsStatus() == null ? null : query.getIsStatus().shortValue())
                        .orderByAsc(SysDictData::getDictSort)
                        .orderByAsc(SysDictData::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toDataVO).toList());
    }

    @Override
    public void addType(DictTypeDTO dto) {
        require(dto != null, "字典类型参数不能为空");
        require(hasText(dto.getDictName()), "字典名称不能为空");
        require(hasText(dto.getDictType()), "字典类型不能为空");
        ensureDictTypeUnique(dto.getDictType(), null);
        SysDictType entity = new SysDictType();
        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setIsStatus(toStatus(dto.getIsStatus()));
        entity.setRemark(dto.getRemark());
        typeMapper.insert(entity);
        cacheService.refreshDict(entity.getDictType());
    }

    @Override
    public void editType(DictTypeDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "字典类型ID不能为空");
        SysDictType entity = requireType(id);
        require(hasText(dto.getDictName()), "字典名称不能为空");
        require(hasText(dto.getDictType()), "字典类型不能为空");
        ensureDictTypeUnique(dto.getDictType(), id);
        String oldDictType = entity.getDictType();
        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setIsStatus(toStatus(dto.getIsStatus()));
        entity.setRemark(dto.getRemark());
        typeMapper.updateById(entity);
        if (!Objects.equals(oldDictType, dto.getDictType())) {
            cacheService.refreshDict(oldDictType);
        }
        cacheService.refreshDict(dto.getDictType());
    }

    @Override
    public void deleteType(String id) {
        Long typeId = parseRequiredId(id, "字典类型ID不能为空");
        SysDictType type = requireType(typeId);
        require(dataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, type.getDictType())) == 0, "字典类型下存在数据，不允许删除");
        typeMapper.deleteById(typeId);
        cacheService.refreshDict(type.getDictType());
    }

    @Override
    public void addData(DictDataDTO dto) {
        require(dto != null, "字典数据参数不能为空");
        require(hasText(dto.getDictType()), "字典类型不能为空");
        require(hasText(dto.getDictLabel()), "字典标签不能为空");
        require(hasText(dto.getDictValue()), "字典键值不能为空");
        require(typeMapper.selectCount(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, dto.getDictType())) > 0,
                "字典类型不存在");
        SysDictData entity = fillData(new SysDictData(), dto);
        dataMapper.insert(entity);
        cacheService.refreshDict(entity.getDictType());
    }

    @Override
    public void editData(DictDataDTO dto) {
        Long id = parseRequiredId(dto == null ? null : dto.getId(), "字典数据ID不能为空");
        SysDictData entity = dataMapper.selectById(id);
        require(entity != null, "字典数据不存在");
        String oldDictType = entity.getDictType();
        dataMapper.updateById(fillData(entity, dto));
        if (!Objects.equals(oldDictType, dto.getDictType())) {
            cacheService.refreshDict(oldDictType);
        }
        cacheService.refreshDict(dto.getDictType());
    }

    @Override
    public void deleteData(String id) {
        SysDictData entity = dataMapper.selectById(parseRequiredId(id, "字典数据ID不能为空"));
        require(entity != null, "字典数据不存在");
        dataMapper.deleteById(entity.getId());
        cacheService.refreshDict(entity.getDictType());
    }

    private SysDictData fillData(SysDictData entity, DictDataDTO dto) {
        entity.setDictType(dto.getDictType());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setDictSort(dto.getDictSort() == null ? 0 : dto.getDictSort());
        entity.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault().shortValue());
        entity.setIsStatus(toStatus(dto.getIsStatus()));
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private DictTypeVO toTypeVO(SysDictType entity) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setDictName(entity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setIsStatus(entity.getIsStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private DictDataVO toDataVO(SysDictData entity) {
        DictDataVO vo = new DictDataVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setDictSort(entity.getDictSort());
        vo.setIsDefault(entity.getIsDefault());
        vo.setIsStatus(entity.getIsStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private SysDictType requireType(Long id) {
        SysDictType type = typeMapper.selectById(id);
        if (type == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "字典类型不存在");
        }
        return type;
    }

    private void ensureDictTypeUnique(String dictType, Long selfId) {
        SysDictType existed = typeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType)
                .last("limit 1"));
        require(existed == null || Objects.equals(existed.getId(), selfId), "字典类型已存在");
    }
}
