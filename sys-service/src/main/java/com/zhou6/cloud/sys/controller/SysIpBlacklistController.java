package com.zhou6.cloud.sys.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.IpBlacklistDTO;
import com.zhou6.cloud.sys.dto.IpBlacklistDeleteDTO;
import com.zhou6.cloud.sys.dto.IpBlacklistQueryDTO;
import com.zhou6.cloud.sys.service.SysIpBlacklistService;
import com.zhou6.cloud.sys.vo.IpBlacklistVO;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "IP 黑名单管理", description = "维护网关入口的 IP 访问封禁规则")
@RestController
@RequestMapping(SysApiPathConstants.IP_BLACKLIST)
public class SysIpBlacklistController {

    private final SysIpBlacklistService ipBlacklistService;

    public SysIpBlacklistController(SysIpBlacklistService ipBlacklistService) {
        this.ipBlacklistService = ipBlacklistService;
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询 IP 黑名单")
    public R<PageResponse<IpBlacklistVO>> page(@RequestBody IpBlacklistQueryDTO dto) {
        return R.ok(ipBlacklistService.page(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "新增 IP 黑名单")
    public R<Void> add(@RequestBody IpBlacklistDTO dto) {
        ipBlacklistService.add(dto);
        return R.ok(null);
    }

    @PostMapping("/edit")
    @Operation(summary = "修改 IP 黑名单")
    public R<Void> edit(@RequestBody IpBlacklistDTO dto) {
        ipBlacklistService.edit(dto);
        return R.ok(null);
    }

    @PostMapping("/delete")
    @Operation(summary = "批量删除 IP 黑名单")
    public R<Void> delete(@RequestBody IpBlacklistDeleteDTO dto) {
        ipBlacklistService.delete(dto == null ? null : dto.getIds());
        return R.ok(null);
    }
}
