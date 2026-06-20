package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.constant.UserApiPathConstants;
import com.zhou6.cloud.user.dto.ExternalSystemChangeStatusDTO;
import com.zhou6.cloud.user.dto.ExternalSystemIdDTO;
import com.zhou6.cloud.user.dto.ExternalSystemQueryDTO;
import com.zhou6.cloud.user.dto.ExternalSystemSaveDTO;
import com.zhou6.cloud.user.service.ExternalSystemService;
import com.zhou6.cloud.user.vo.ExternalSystemVO;
import com.zhou6.cloud.user.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 外部系统管理接口。 */
@Tag(name = "外部系统管理", description = "维护一站式服务接入的外部系统")
@RestController
@RequestMapping(UserApiPathConstants.EXTERNAL_SYSTEM)
public class ExternalSystemController {

    private final ExternalSystemService externalSystemService;

    public ExternalSystemController(ExternalSystemService externalSystemService) {
        this.externalSystemService = externalSystemService;
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询外部系统")
    public R<PageResponse<ExternalSystemVO>> page(@RequestBody ExternalSystemQueryDTO dto) {
        return R.ok(externalSystemService.page(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "新增外部系统")
    public R<Void> add(@RequestBody ExternalSystemSaveDTO dto) {
        externalSystemService.add(dto);
        return R.ok(null);
    }

    @PostMapping("/edit")
    @Operation(summary = "修改外部系统")
    public R<Void> edit(@RequestBody ExternalSystemSaveDTO dto) {
        externalSystemService.edit(dto);
        return R.ok(null);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除外部系统", description = "存在关联角色时不允许删除")
    public R<Void> delete(@RequestBody ExternalSystemIdDTO dto) {
        externalSystemService.delete(dto);
        return R.ok(null);
    }

    @PostMapping("/changeStatus")
    @Operation(summary = "修改外部系统状态")
    public R<Void> changeStatus(@RequestBody ExternalSystemChangeStatusDTO dto) {
        externalSystemService.changeStatus(dto);
        return R.ok(null);
    }

    @PostMapping("/listEnabled")
    @Operation(summary = "查询启用的外部系统", description = "供角色所属系统下拉框使用")
    public R<List<ExternalSystemVO>> listEnabled() {
        return R.ok(externalSystemService.listEnabled());
    }
}
