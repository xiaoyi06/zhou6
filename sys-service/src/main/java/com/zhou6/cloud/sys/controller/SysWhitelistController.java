package com.zhou6.cloud.sys.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.WhitelistDeleteDTO;
import com.zhou6.cloud.sys.dto.WhitelistDTO;
import com.zhou6.cloud.sys.dto.WhitelistQueryDTO;
import com.zhou6.cloud.sys.entity.SysWhitelist;
import com.zhou6.cloud.sys.service.SysWhitelistService;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统白名单管理", description = "维护 IP、用户、路由白名单")
@RestController
@RequestMapping(SysApiPathConstants.WHITELIST)
public class SysWhitelistController {

    private final SysWhitelistService whitelistService;

    public SysWhitelistController(SysWhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询白名单")
    public R<PageResponse<SysWhitelist>> page(@RequestBody WhitelistQueryDTO dto) {
        return R.ok(whitelistService.page(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "新增白名单")
    public R<Void> add(@RequestBody WhitelistDTO dto) {
        whitelistService.add(dto);
        return R.ok(null);
    }

    @PostMapping("/edit")
    @Operation(summary = "修改白名单")
    public R<Void> edit(@RequestBody WhitelistDTO dto) {
        whitelistService.edit(dto);
        return R.ok(null);
    }

    @PostMapping("/delete")
    @Operation(summary = "批量删除白名单")
    public R<Void> delete(@RequestBody WhitelistDeleteDTO dto) {
        whitelistService.delete(dto == null ? null : dto.getIds());
        return R.ok(null);
    }
}
