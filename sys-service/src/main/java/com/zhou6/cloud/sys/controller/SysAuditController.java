package com.zhou6.cloud.sys.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.LoginLogDTO;
import com.zhou6.cloud.sys.dto.PageQueryDTO;
import com.zhou6.cloud.sys.entity.SysLoginLog;
import com.zhou6.cloud.sys.service.SysAuditService;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统审计日志", description = "登录日志异步投递和查询")
@RestController
@RequestMapping(SysApiPathConstants.AUDIT)
public class SysAuditController {

    private final SysAuditService auditService;

    public SysAuditController(SysAuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/loginLog")
    @Operation(summary = "异步投递登录日志")
    public R<Void> loginLog(@RequestBody LoginLogDTO dto) {
        auditService.publishLoginLog(dto);
        return R.ok(null);
    }

    @PostMapping("/loginLogPage")
    @Operation(summary = "分页查询登录日志")
    public R<PageResponse<SysLoginLog>> loginLogPage(@RequestBody PageQueryDTO dto) {
        return R.ok(auditService.loginLogPage(dto));
    }
}
