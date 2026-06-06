package com.zhou6.cloud.workflow.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.workflow.constant.WorkflowApiPathConstants;
import com.zhou6.cloud.workflow.dto.ProcessStartDTO;
import com.zhou6.cloud.workflow.dto.ProcessTerminateDTO;
import com.zhou6.cloud.workflow.dto.TaskCompleteDTO;
import com.zhou6.cloud.workflow.service.WorkflowProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工作流内部接口", description = "提供业务微服务调用的流程启动、办理和终止接口")
@RestController
@RequestMapping(WorkflowApiPathConstants.INNER)
public class WorkflowInnerController {

    private final WorkflowProcessService workflowProcessService;

    public WorkflowInnerController(WorkflowProcessService workflowProcessService) {
        this.workflowProcessService = workflowProcessService;
    }

    /**
     * 启动流程实例。
     *
     * @param dto 流程启动参数
     * @return 流程实例 ID
     */
    @PostMapping("/start")
    @Operation(summary = "启动流程实例", description = "按流程定义 Key 和业务唯一号启动流程实例")
    public R<String> start(@RequestBody ProcessStartDTO dto) {
        return R.ok(workflowProcessService.startProcess(dto));
    }

    /**
     * 办理流程任务。
     *
     * @param dto 任务办理参数
     * @return 空响应
     */
    @PostMapping("/complete")
    @Operation(summary = "办理流程任务", description = "提交任务变量和审批意见并完成任务")
    public R<Void> complete(@RequestBody TaskCompleteDTO dto) {
        workflowProcessService.completeTask(dto);
        return R.ok(null);
    }

    /**
     * 终止流程实例，并广播业务状态事件。
     *
     * @param dto 流程终止参数
     * @return 空响应
     */
    @PostMapping("/terminate")
    @Operation(summary = "终止流程实例", description = "按业务唯一号终止流程实例并发布业务状态事件")
    public R<Void> terminate(@RequestBody ProcessTerminateDTO dto) {
        workflowProcessService.terminateProcess(dto);
        return R.ok(null);
    }
}
