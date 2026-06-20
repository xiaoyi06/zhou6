package com.zhou6.cloud.workflow.service;

import com.zhou6.cloud.workflow.dto.DefinitionIdDTO;
import com.zhou6.cloud.workflow.dto.DefinitionQueryDTO;
import com.zhou6.cloud.workflow.dto.DeployDTO;
import com.zhou6.cloud.workflow.dto.DeploymentIdDTO;
import com.zhou6.cloud.workflow.dto.ModelIdDTO;
import com.zhou6.cloud.workflow.dto.ModelQueryDTO;
import com.zhou6.cloud.workflow.dto.ModelSaveDTO;
import com.zhou6.cloud.workflow.vo.DefinitionVO;
import com.zhou6.cloud.workflow.vo.DeployResultVO;
import com.zhou6.cloud.workflow.vo.ModelDetailVO;
import com.zhou6.cloud.workflow.vo.ModelVO;
import com.zhou6.cloud.workflow.vo.PageResponse;

/**
 * 流程配置管理服务，基于 Flowable RepositoryService 实现。
 */
public interface WorkflowConfigService {

    // ===== 模型管理（ACT_RE_MODEL） =====

    PageResponse<ModelVO> pageModel(ModelQueryDTO dto);

    ModelDetailVO getModel(ModelIdDTO dto);

    String saveModel(ModelSaveDTO dto);

    void deleteModel(ModelIdDTO dto);

    // ===== 部署（模型 → 引擎） =====

    DeployResultVO deploy(DeployDTO dto);

    // ===== 流程定义管理（ACT_RE_PROCDEF） =====

    PageResponse<DefinitionVO> pageDefinition(DefinitionQueryDTO dto);

    void suspendDefinition(DefinitionIdDTO dto);

    void activateDefinition(DefinitionIdDTO dto);

    // ===== 部署管理（ACT_RE_DEPLOYMENT） =====

    void deleteDeployment(DeploymentIdDTO dto);
}
