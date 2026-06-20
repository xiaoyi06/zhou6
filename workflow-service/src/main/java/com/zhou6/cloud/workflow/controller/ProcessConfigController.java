package com.zhou6.cloud.workflow.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.workflow.constant.WorkflowApiPathConstants;
import com.zhou6.cloud.workflow.dto.DefinitionIdDTO;
import com.zhou6.cloud.workflow.dto.DefinitionQueryDTO;
import com.zhou6.cloud.workflow.dto.DeployDTO;
import com.zhou6.cloud.workflow.dto.DeploymentIdDTO;
import com.zhou6.cloud.workflow.dto.ModelIdDTO;
import com.zhou6.cloud.workflow.dto.ModelQueryDTO;
import com.zhou6.cloud.workflow.dto.ModelSaveDTO;
import com.zhou6.cloud.workflow.service.WorkflowConfigService;
import com.zhou6.cloud.workflow.vo.DefinitionVO;
import com.zhou6.cloud.workflow.vo.DeployResultVO;
import com.zhou6.cloud.workflow.vo.ModelDetailVO;
import com.zhou6.cloud.workflow.vo.ModelVO;
import com.zhou6.cloud.workflow.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程配置管理接口，直接操作 Flowable 内置的模型和流程定义。
 */
@Tag(name = "流程配置管理", description = "模型管理（设计时）、部署、流程定义启停、部署删除")
@RestController
@RequestMapping(WorkflowApiPathConstants.CONFIG)
public class ProcessConfigController {

    private final WorkflowConfigService workflowConfigService;

    public ProcessConfigController(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
    }

    // ==================== 模型管理 ====================

    @PostMapping("/model/page")
    @Operation(summary = "分页查询模型", description = "查询 ACT_RE_MODEL 中的流程模型列表")
    public R<PageResponse<ModelVO>> pageModel(@RequestBody ModelQueryDTO dto) {
        return R.ok(workflowConfigService.pageModel(dto));
    }

    @PostMapping("/model/getById")
    @Operation(summary = "查询模型详情", description = "获取模型元信息及 BPMN XML")
    public R<ModelDetailVO> getModel(@RequestBody ModelIdDTO dto) {
        return R.ok(workflowConfigService.getModel(dto));
    }

    @PostMapping("/model/save")
    @Operation(summary = "保存模型", description = "新增或修改流程模型，同时保存 BPMN XML")
    public R<String> saveModel(@RequestBody ModelSaveDTO dto) {
        return R.ok(workflowConfigService.saveModel(dto));
    }

    @PostMapping("/model/delete")
    @Operation(summary = "删除模型", description = "删除 ACT_RE_MODEL 中的指定模型")
    public R<Void> deleteModel(@RequestBody ModelIdDTO dto) {
        workflowConfigService.deleteModel(dto);
        return R.ok(null);
    }

    // ==================== 部署 ====================

    @PostMapping("/deploy")
    @Operation(summary = "部署流程", description = "将模型部署到 Flowable 引擎，生成流程定义")
    public R<DeployResultVO> deploy(@RequestBody DeployDTO dto) {
        return R.ok(workflowConfigService.deploy(dto));
    }

    // ==================== 流程定义管理 ====================

    @PostMapping("/definition/page")
    @Operation(summary = "分页查询流程定义", description = "查询 ACT_RE_PROCDEF 中的已部署流程定义")
    public R<PageResponse<DefinitionVO>> pageDefinition(@RequestBody DefinitionQueryDTO dto) {
        return R.ok(workflowConfigService.pageDefinition(dto));
    }

    @PostMapping("/definition/suspend")
    @Operation(summary = "挂起流程定义", description = "挂起后无法发起新流程实例")
    public R<Void> suspendDefinition(@RequestBody DefinitionIdDTO dto) {
        workflowConfigService.suspendDefinition(dto);
        return R.ok(null);
    }

    @PostMapping("/definition/activate")
    @Operation(summary = "激活流程定义", description = "恢复已挂起的流程定义")
    public R<Void> activateDefinition(@RequestBody DefinitionIdDTO dto) {
        workflowConfigService.activateDefinition(dto);
        return R.ok(null);
    }

    // ==================== 部署管理 ====================

    @PostMapping("/deployment/delete")
    @Operation(summary = "删除部署", description = "删除部署及关联的流程定义和运行时数据")
    public R<Void> deleteDeployment(@RequestBody DeploymentIdDTO dto) {
        workflowConfigService.deleteDeployment(dto);
        return R.ok(null);
    }
}
