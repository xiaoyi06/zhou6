package com.zhou6.cloud.sys.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.MenuUsageDTO;
import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.entity.SysMenuUsageStat;
import com.zhou6.cloud.sys.service.SysMenuUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "常用菜单统计", description = "记录并查询当前用户经常使用的菜单")
@RestController
@RequestMapping(SysApiPathConstants.MENU_USAGE)
public class SysMenuUsageController {

    private final SysMenuUsageService menuUsageService;

    public SysMenuUsageController(SysMenuUsageService menuUsageService) {
        this.menuUsageService = menuUsageService;
    }

    @PostMapping("/record")
    @Operation(summary = "记录菜单使用行为")
    public R<Void> record(@RequestBody MenuUsageDTO dto) {
        menuUsageService.record(dto);
        return R.ok(null);
    }

    @PostMapping("/frequent")
    @Operation(summary = "查询用户常用菜单")
    public R<List<SysMenuUsageStat>> frequent(@RequestBody MenuUsageQueryDTO dto) {
        return R.ok(menuUsageService.frequent(dto));
    }
}
