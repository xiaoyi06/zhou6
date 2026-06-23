package com.zhou6.cloud.workflow.controller;

import java.util.List;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.workflow.constant.WorkflowApiPathConstants;
import com.zhou6.cloud.workflow.service.WorkflowProcessService;
import com.zhou6.cloud.workflow.dto.TaskPageQueryDTO;
import com.zhou6.cloud.workflow.dto.InitiatedProcessPageQueryDTO;
import com.zhou6.cloud.workflow.vo.HistoricTaskVO;
import com.zhou6.cloud.workflow.vo.TaskVO;
import com.zhou6.cloud.workflow.vo.WorkflowHomeSummaryVO;
import com.zhou6.cloud.workflow.vo.PageResponse;
import com.zhou6.cloud.workflow.vo.ProcessInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "流程工作台", description = "提供待办、已办等流程工作台查询接口")
@RestController
@RequestMapping(WorkflowApiPathConstants.WORKFLOW + "/process")
public class ProcessController {

    private final WorkflowProcessService workflowProcessService;

    public ProcessController(WorkflowProcessService workflowProcessService) {
        this.workflowProcessService = workflowProcessService;
    }

    /**
     * 查询当前用户待办任务。
     *
     * @param userId 用户 ID
     * @return 待办任务列表
     */
    @GetMapping("/todo")
    @Operation(summary = "查询待办任务", description = "按用户ID查询候选或已分配给当前用户的待办任务")
    public R<List<TaskVO>> todo(@RequestParam String userId) {
        return R.ok(workflowProcessService.listTodoTasks(userId));
    }

    /**
     * 查询当前用户已办任务。
     *
     * @param userId 用户 ID
     * @return 已办任务列表
     */
    @GetMapping("/done")
    @Operation(summary = "查询已办任务", description = "按用户ID查询已完成的历史任务")
    public R<List<HistoricTaskVO>> done(@RequestParam String userId) {
        return R.ok(workflowProcessService.listDoneTasks(userId));
    }
    @PostMapping("/todo/page") @Operation(summary = "分页查询待办")
    public R<PageResponse<TaskVO>> todoPage(@RequestBody TaskPageQueryDTO dto) { return R.ok(workflowProcessService.pageTodoTasks(dto)); }
    @PostMapping("/done/page") @Operation(summary = "分页查询已办")
    public R<PageResponse<HistoricTaskVO>> donePage(@RequestBody TaskPageQueryDTO dto) { return R.ok(workflowProcessService.pageDoneTasks(dto)); }
    @PostMapping("/initiated/page") @Operation(summary = "分页查询我发起的流程")
    public R<PageResponse<ProcessInstanceVO>> initiatedPage(@RequestBody InitiatedProcessPageQueryDTO dto) { return R.ok(workflowProcessService.pageInitiatedProcesses(dto)); }

    /**
     * 查询当前用户首页流程统计。
     *
     * @return 首页统计数据
     */
    @GetMapping("/home/summary")
    @Operation(summary = "查询首页流程统计", description = "查询当前用户待办、待审核、已审核、驳回废除数量和最近3天折线图数据")
    public R<WorkflowHomeSummaryVO> homeSummary() {
        return R.ok(workflowProcessService.getHomeSummary());
    }
}
