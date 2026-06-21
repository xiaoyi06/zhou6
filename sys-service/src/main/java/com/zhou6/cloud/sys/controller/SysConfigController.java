package com.zhou6.cloud.sys.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.ConfigDTO;
import com.zhou6.cloud.sys.dto.ConfigQueryDTO;
import com.zhou6.cloud.sys.dto.IdDTO;
import com.zhou6.cloud.sys.service.SysConfigService;
import com.zhou6.cloud.sys.vo.ConfigVO;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统配置管理", description = "维护系统控制开关和 JSONB 配置")
@RestController
@RequestMapping(SysApiPathConstants.CONFIG)
public class SysConfigController {

    private final SysConfigService configService;

    public SysConfigController(SysConfigService configService) {
        this.configService = configService;
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询系统配置")
    public R<PageResponse<ConfigVO>> page(@RequestBody ConfigQueryDTO dto) {
        return R.ok(configService.page(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "新增系统配置")
    public R<Void> add(@RequestBody ConfigDTO dto) {
        configService.add(dto);
        return R.ok(null);
    }

    @PostMapping("/edit")
    @Operation(summary = "修改系统配置")
    public R<Void> edit(@RequestBody ConfigDTO dto) {
        configService.edit(dto);
        return R.ok(null);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除系统配置")
    public R<Void> delete(@RequestBody IdDTO dto) {
        configService.delete(dto == null ? null : dto.getId());
        return R.ok(null);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新指定配置缓存")
    public R<Void> refresh(@RequestBody ConfigDTO dto) {
        configService.refreshCache(dto == null ? null : dto.getConfigKey());
        return R.ok(null);
    }
}
