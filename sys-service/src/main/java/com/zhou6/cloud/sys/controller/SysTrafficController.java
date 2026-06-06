package com.zhou6.cloud.sys.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.TrafficQueryDTO;
import com.zhou6.cloud.sys.entity.SysTrafficStat;
import com.zhou6.cloud.sys.service.SysTrafficService;
import com.zhou6.cloud.sys.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统流量监控", description = "查询流量聚合数据")
@RestController
@RequestMapping(SysApiPathConstants.TRAFFIC)
public class SysTrafficController {

    private final SysTrafficService trafficService;

    public SysTrafficController(SysTrafficService trafficService) {
        this.trafficService = trafficService;
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询流量统计")
    public R<PageResponse<SysTrafficStat>> page(@RequestBody TrafficQueryDTO dto) {
        return R.ok(trafficService.page(dto));
    }
}
