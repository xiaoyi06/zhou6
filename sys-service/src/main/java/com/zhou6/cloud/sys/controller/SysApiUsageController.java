package com.zhou6.cloud.sys.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.dto.MenuUsageSummaryQueryDTO;
import com.zhou6.cloud.sys.dto.MenuUsageUserQueryDTO;
import com.zhou6.cloud.sys.service.SysApiUsageService;
import com.zhou6.cloud.sys.vo.ApiUsageStatVO;
import com.zhou6.cloud.sys.vo.MenuUsageSummaryVO;
import com.zhou6.cloud.sys.vo.MenuUsageUserVO;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "应用/菜单调用统计", description = "查询网关自动追踪的用户常用功能")
@RestController
@RequestMapping(SysApiPathConstants.API_USAGE)
public class SysApiUsageController {

    private final SysApiUsageService apiUsageService;

    public SysApiUsageController(SysApiUsageService apiUsageService) {
        this.apiUsageService = apiUsageService;
    }

    @PostMapping("/frequent")
    @Operation(summary = "查询用户常用功能")
    public R<List<ApiUsageStatVO>> frequent(@RequestBody MenuUsageQueryDTO dto) {
        return R.ok(apiUsageService.frequent(dto));
    }

    @PostMapping("/menuPage")
    @Operation(summary = "管理员分页查询菜单访问汇总")
    public R<PageResponse<MenuUsageSummaryVO>> menuPage(@RequestBody(required = false) MenuUsageSummaryQueryDTO dto) {
        return R.ok(apiUsageService.menuPage(dto));
    }

    @PostMapping("/menuUserPage")
    @Operation(summary = "管理员分页查询菜单访问人员")
    public R<PageResponse<MenuUsageUserVO>> menuUserPage(@RequestBody MenuUsageUserQueryDTO dto) {
        return R.ok(apiUsageService.menuUserPage(dto));
    }

    @PostMapping("/sync")
    @Operation(summary = "立即同步菜单访问统计")
    public R<Void> sync() {
        apiUsageService.sync();
        return R.ok(null);
    }

    @PostMapping("/clear")
    @Operation(summary = "清空菜单访问统计")
    public R<Void> clear() {
        apiUsageService.clear();
        return R.ok(null);
    }
}
