package com.zhou6.cloud.sys.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.MenuUsageQueryDTO;
import com.zhou6.cloud.sys.entity.SysApiUsageStat;
import com.zhou6.cloud.sys.service.SysApiUsageService;
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
    public R<List<SysApiUsageStat>> frequent(@RequestBody MenuUsageQueryDTO dto) {
        return R.ok(apiUsageService.frequent(dto));
    }
}
