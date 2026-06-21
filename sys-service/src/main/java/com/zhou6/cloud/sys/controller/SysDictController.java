package com.zhou6.cloud.sys.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.DictDataDTO;
import com.zhou6.cloud.sys.dto.DictQueryDTO;
import com.zhou6.cloud.sys.dto.DictTypeDTO;
import com.zhou6.cloud.sys.dto.IdDTO;
import com.zhou6.cloud.sys.service.SysDictService;
import com.zhou6.cloud.sys.vo.DictDataVO;
import com.zhou6.cloud.sys.vo.DictTypeVO;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统字典管理", description = "维护系统字典类型和字典数据")
@RestController
@RequestMapping(SysApiPathConstants.DICT)
public class SysDictController {

    private final SysDictService dictService;

    public SysDictController(SysDictService dictService) {
        this.dictService = dictService;
    }

    @PostMapping("/typePage")
    @Operation(summary = "分页查询字典类型")
    public R<PageResponse<DictTypeVO>> typePage(@RequestBody DictQueryDTO dto) {
        return R.ok(dictService.typePage(dto));
    }

    @PostMapping("/dataList")
    @Operation(summary = "查询字典数据列表")
    public R<List<DictDataVO>> dataList(@RequestBody DictQueryDTO dto) {
        return R.ok(dictService.dataList(dto));
    }

    @PostMapping("/dataPage")
    @Operation(summary = "分页查询字典数据")
    public R<PageResponse<DictDataVO>> dataPage(@RequestBody DictQueryDTO dto) {
        return R.ok(dictService.dataPage(dto));
    }

    @PostMapping("/typeAdd")
    @Operation(summary = "新增字典类型")
    public R<Void> typeAdd(@RequestBody DictTypeDTO dto) {
        dictService.addType(dto);
        return R.ok(null);
    }

    @PostMapping("/typeEdit")
    @Operation(summary = "修改字典类型")
    public R<Void> typeEdit(@RequestBody DictTypeDTO dto) {
        dictService.editType(dto);
        return R.ok(null);
    }

    @PostMapping("/typeDelete")
    @Operation(summary = "删除字典类型")
    public R<Void> typeDelete(@RequestBody IdDTO dto) {
        dictService.deleteType(dto == null ? null : dto.getId());
        return R.ok(null);
    }

    @PostMapping("/dataAdd")
    @Operation(summary = "新增字典数据")
    public R<Void> dataAdd(@RequestBody DictDataDTO dto) {
        dictService.addData(dto);
        return R.ok(null);
    }

    @PostMapping("/dataEdit")
    @Operation(summary = "修改字典数据")
    public R<Void> dataEdit(@RequestBody DictDataDTO dto) {
        dictService.editData(dto);
        return R.ok(null);
    }

    @PostMapping("/dataDelete")
    @Operation(summary = "删除字典数据")
    public R<Void> dataDelete(@RequestBody IdDTO dto) {
        dictService.deleteData(dto == null ? null : dto.getId());
        return R.ok(null);
    }
}
