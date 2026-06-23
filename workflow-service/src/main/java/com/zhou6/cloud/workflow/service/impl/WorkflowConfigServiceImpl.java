package com.zhou6.cloud.workflow.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
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
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;

/**
 * 流程配置管理业务实现，直接操作 Flowable ACT_RE_* 表。
 */
@Service
public class WorkflowConfigServiceImpl implements WorkflowConfigService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final RepositoryService repositoryService;

    public WorkflowConfigServiceImpl(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    // ==================== 模型管理 ====================

    @Override
    public PageResponse<ModelVO> pageModel(ModelQueryDTO dto) {
        ModelQueryDTO query = dto == null ? new ModelQueryDTO() : dto;
        ModelQuery modelQuery = repositoryService.createModelQuery();
        if (hasText(query.getName())) {
            modelQuery.modelNameLike(like(query.getName()));
        }
        if (hasText(query.getKey())) {
            modelQuery.modelKey(query.getKey().trim());
        }
        if (hasText(query.getCategory())) {
            modelQuery.modelCategory(query.getCategory().trim());
        }
        modelQuery.orderByCreateTime().desc();
        long total = modelQuery.count();
        long offset = (pageNum(query) - 1) * pageSize(query);
        return new PageResponse<>(total, pageNum(query), pageSize(query),
                modelQuery.listPage((int) offset, (int) pageSize(query))
                        .stream().map(this::toModelVo).toList());
    }

    @Override
    public ModelDetailVO getModel(ModelIdDTO dto) {
        require(dto != null && hasText(dto.getId()), "模型 ID 不能为空");
        Model model = getRequiredModel(dto.getId());
        ModelDetailVO vo = new ModelDetailVO();
        vo.setId(model.getId());
        vo.setName(model.getName());
        vo.setKey(model.getKey());
        vo.setCategory(model.getCategory());
        vo.setVersion(model.getVersion());
        vo.setCreateTime(format(model.getCreateTime()));
        vo.setLastUpdateTime(format(model.getLastUpdateTime()));
        vo.setDeploymentId(model.getDeploymentId());
        byte[] source = repositoryService.getModelEditorSource(model.getId());
        if (source != null) {
            vo.setBpmnXml(new String(source, StandardCharsets.UTF_8));
        }
        return vo;
    }

    @Override
    public String saveModel(ModelSaveDTO dto) {
        require(dto != null, "模型参数不能为空");
        validateBpmnMetadata(dto);
        String modelId = dto.getId();
        if (hasText(modelId)) {
            // 修改
            Model model = getRequiredModel(modelId);
            model.setName(dto.getName());
            model.setKey(dto.getKey());
            model.setCategory(dto.getCategory());
            repositoryService.saveModel(model);
        } else {
            // 新增
            require(hasText(dto.getKey()), "模型 Key 不能为空");
            require(hasText(dto.getName()), "模型名称不能为空");
            Model model = repositoryService.newModel();
            model.setName(dto.getName());
            model.setKey(dto.getKey());
            model.setCategory(dto.getCategory());
            model.setVersion(1);
            repositoryService.saveModel(model);
            modelId = model.getId();
        }
        if (hasText(dto.getBpmnXml())) {
            repositoryService.addModelEditorSource(modelId, dto.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        }
        return modelId;
    }

    @Override
    public void deleteModel(ModelIdDTO dto) {
        require(dto != null && hasText(dto.getId()), "模型 ID 不能为空");
        getRequiredModel(dto.getId());
        repositoryService.deleteModel(dto.getId());
    }

    // ==================== 部署 ====================

    @Override
    public DeployResultVO deploy(DeployDTO dto) {
        require(dto != null && hasText(dto.getModelId()), "模型 ID 不能为空");
        Model model = getRequiredModel(dto.getModelId());
        byte[] bpmnBytes = repositoryService.getModelEditorSource(model.getId());
        require(bpmnBytes != null && bpmnBytes.length > 0, "BPMN XML 为空，请先保存模型设计");
        String resourceName = (hasText(model.getKey()) ? model.getKey() : model.getId()) + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .key(model.getKey())
                .category(model.getCategory())
                .addString(resourceName, new String(bpmnBytes, StandardCharsets.UTF_8))
                .deploy();
        // 回填模型上的 deploymentId
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        DeployResultVO vo = new DeployResultVO();
        vo.setDeploymentId(deployment.getId());
        vo.setDefinitionId(definition != null ? definition.getId() : null);
        vo.setDefinitionKey(definition != null ? definition.getKey() : null);
        vo.setVersion(definition != null ? definition.getVersion() : 1);
        return vo;
    }

    // ==================== 流程定义管理 ====================

    @Override
    public PageResponse<DefinitionVO> pageDefinition(DefinitionQueryDTO dto) {
        DefinitionQueryDTO query = dto == null ? new DefinitionQueryDTO() : dto;
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();
        if (hasText(query.getProcessKey())) {
            definitionQuery.processDefinitionKey(query.getProcessKey().trim());
        }
        if (hasText(query.getCategory())) {
            definitionQuery.processDefinitionCategory(query.getCategory().trim());
        }
        definitionQuery.orderByProcessDefinitionVersion().desc();
        long total = definitionQuery.count();
        long offset = (pageNum(query) - 1) * pageSize(query);
        return new PageResponse<>(total, pageNum(query), pageSize(query),
                definitionQuery.listPage((int) offset, (int) pageSize(query))
                        .stream().map(this::toDefinitionVo).toList());
    }

    @Override
    public void suspendDefinition(DefinitionIdDTO dto) {
        require(dto != null && hasText(dto.getId()), "流程定义 ID 不能为空");
        repositoryService.suspendProcessDefinitionById(dto.getId());
    }

    @Override
    public void activateDefinition(DefinitionIdDTO dto) {
        require(dto != null && hasText(dto.getId()), "流程定义 ID 不能为空");
        repositoryService.activateProcessDefinitionById(dto.getId());
    }

    // ==================== 部署管理 ====================

    @Override
    public void deleteDeployment(DeploymentIdDTO dto) {
        require(dto != null && hasText(dto.getId()), "部署 ID 不能为空");
        repositoryService.deleteDeployment(dto.getId(), true);
    }

    // ==================== 私有方法 ====================

    private Model getRequiredModel(String modelId) {
        Model model = repositoryService.getModel(modelId);
        require(model != null, "模型不存在");
        return model;
    }

    private ModelVO toModelVo(Model model) {
        ModelVO vo = new ModelVO();
        vo.setId(model.getId());
        vo.setName(model.getName());
        vo.setKey(model.getKey());
        vo.setCategory(model.getCategory());
        vo.setVersion(model.getVersion());
        vo.setCreateTime(format(model.getCreateTime()));
        vo.setLastUpdateTime(format(model.getLastUpdateTime()));
        vo.setDeploymentId(model.getDeploymentId());
        return vo;
    }

    private DefinitionVO toDefinitionVo(ProcessDefinition def) {
        DefinitionVO vo = new DefinitionVO();
        vo.setId(def.getId());
        vo.setKey(def.getKey());
        vo.setName(def.getName());
        vo.setCategory(def.getCategory());
        vo.setVersion(def.getVersion());
        vo.setDeploymentId(def.getDeploymentId());
        vo.setSuspended(def.isSuspended());
        vo.setTenantId(def.getTenantId());
        return vo;
    }

    private String like(String value) {
        return hasText(value) ? "%" + value.trim() + "%" : null;
    }

    private void validateBpmnMetadata(ModelSaveDTO dto) {
        if (!hasText(dto.getBpmnXml())) {
            return;
        }
        require(hasText(dto.getKey()), "模型 Key 不能为空");
        require(hasText(dto.getName()), "模型名称不能为空");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(dto.getBpmnXml())));
            NodeList processes = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");
            require(processes.getLength() == 1, "BPMN XML 必须且只能包含一个流程定义");
            Element process = (Element) processes.item(0);
            String processKey = process.getAttribute("id").trim();
            String processName = process.getAttribute("name").trim();
            require(hasText(processKey), "BPMN 流程 Key 不能为空");
            require(hasText(processName), "BPMN 流程名称不能为空");
            require(dto.getKey().trim().equals(processKey), "模型 Key 必须与 BPMN 流程 Key 一致");
            require(dto.getName().trim().equals(processName), "模型名称必须与 BPMN 流程名称一致");
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "BPMN XML 格式不正确", ex);
        }
    }

    private String format(java.util.Date date) {
        return date == null ? null : date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime().format(DTF);
    }

    private long pageNum(Object dto) {
        if (dto instanceof ModelQueryDTO q) {
            return q.getPageNum() == null || q.getPageNum() < 1 ? 1 : q.getPageNum();
        }
        if (dto instanceof DefinitionQueryDTO q) {
            return q.getPageNum() == null || q.getPageNum() < 1 ? 1 : q.getPageNum();
        }
        return 1;
    }

    private long pageSize(Object dto) {
        if (dto instanceof ModelQueryDTO q) {
            return q.getPageSize() == null || q.getPageSize() < 1 ? 10 : q.getPageSize();
        }
        if (dto instanceof DefinitionQueryDTO q) {
            return q.getPageSize() == null || q.getPageSize() < 1 ? 10 : q.getPageSize();
        }
        return 10;
    }

    private void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
