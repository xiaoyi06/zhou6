package com.zhou6.cloud.workflow.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.context.CurrentLoginUser;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.workflow.dto.ProcessStartDTO;
import com.zhou6.cloud.workflow.dto.ProcessTerminateDTO;
import com.zhou6.cloud.workflow.dto.TaskCompleteDTO;
import com.zhou6.cloud.workflow.dto.TaskPageQueryDTO;
import com.zhou6.cloud.workflow.dto.InitiatedProcessPageQueryDTO;
import com.zhou6.cloud.workflow.handler.WorkflowErrorCode;
import com.zhou6.cloud.workflow.service.WorkflowEventService;
import com.zhou6.cloud.workflow.service.WorkflowProcessService;
import com.zhou6.cloud.workflow.vo.HistoricTaskVO;
import com.zhou6.cloud.workflow.vo.TaskVO;
import com.zhou6.cloud.workflow.vo.WorkflowDailyCountVO;
import com.zhou6.cloud.workflow.vo.WorkflowHomeSummaryVO;
import com.zhou6.cloud.workflow.vo.PageResponse;
import com.zhou6.cloud.workflow.vo.ProcessInstanceVO;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 工作流流程操作 Flowable 实现。
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    private static final int MAX_KEY_LENGTH = 128;
    private static final int MAX_REASON_LENGTH = 500;
    private static final String TERMINATED_STATUS = "TERMINATED";

    private final TaskService taskService;
    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final IdentityService identityService;
    private final WorkflowEventService workflowEventService;
    private final int maxQuerySize;

    public WorkflowProcessServiceImpl(TaskService taskService, HistoryService historyService,
            RuntimeService runtimeService, IdentityService identityService, WorkflowEventService workflowEventService,
            @Value("${zhou6.workflow.query.max-size:100}") int maxQuerySize) {
        this.taskService = taskService;
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.identityService = identityService;
        this.workflowEventService = workflowEventService;
        this.maxQuerySize = Math.max(1, maxQuerySize);
    }

    @Override
    public List<TaskVO> listTodoTasks(String userId) {
        String normalizedUserId = requireText(userId);
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateOrAssigned(normalizedUserId)
                .orderByTaskCreateTime()
                .desc()
                .listPage(0, maxQuerySize);
        Map<String, ProcessInstance> instanceMap = listRuntimeInstanceMap(tasks);
        return tasks.stream()
                .map(task -> toTaskVO(task, instanceMap.get(task.getProcessInstanceId()), null))
                .toList();
    }

    @Override
    public List<HistoricTaskVO> listDoneTasks(String userId) {
        String normalizedUserId = requireText(userId);
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(normalizedUserId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .listPage(0, maxQuerySize);
        Map<String, HistoricProcessInstance> instanceMap = listHistoricInstanceMap(historicTasks);
        return historicTasks.stream()
                .map(task -> new HistoricTaskVO(task.getId(), task.getName(), task.getProcessInstanceId(),
                        historicBusinessKey(task, instanceMap), task.getEndTime()))
                .toList();
    }

    @Override public PageResponse<TaskVO> pageTodoTasks(TaskPageQueryDTO dto) {
        String userId = requireText(dto == null ? null : dto.getUserId()); long page = pageNum(dto == null ? null : dto.getPageNum()); long size = pageSize(dto == null ? null : dto.getPageSize());
        var query = taskService.createTaskQuery().taskCandidateOrAssigned(userId).orderByTaskCreateTime().desc(); List<Task> tasks = query.listPage((int) ((page - 1) * size), (int) size); Map<String, ProcessInstance> map = listRuntimeInstanceMap(tasks);
        Map<String, HistoricProcessInstance> historicMap = listHistoricInstanceMapByIds(tasks.stream().map(Task::getProcessInstanceId).collect(Collectors.toSet()));
        return new PageResponse<>(query.count(), page, size, tasks.stream().map(task -> toTaskVO(task, map.get(task.getProcessInstanceId()), historicMap.get(task.getProcessInstanceId()))).toList());
    }
    @Override public PageResponse<HistoricTaskVO> pageDoneTasks(TaskPageQueryDTO dto) {
        String userId = requireText(dto == null ? null : dto.getUserId()); long page = pageNum(dto == null ? null : dto.getPageNum()); long size = pageSize(dto == null ? null : dto.getPageSize());
        var query = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).finished().orderByHistoricTaskInstanceEndTime().desc(); List<HistoricTaskInstance> tasks = query.listPage((int) ((page - 1) * size), (int) size); Map<String, HistoricProcessInstance> map = listHistoricInstanceMap(tasks);
        return new PageResponse<>(query.count(), page, size, tasks.stream().map(task -> new HistoricTaskVO(task.getId(), task.getName(), task.getProcessInstanceId(), historicBusinessKey(task, map), task.getEndTime())).toList());
    }
    @Override public PageResponse<ProcessInstanceVO> pageInitiatedProcesses(InitiatedProcessPageQueryDTO dto) {
        String status = dto == null || dto.getStatus() == null ? "" : dto.getStatus().trim().toUpperCase(); long page = pageNum(dto == null ? null : dto.getPageNum()); long size = pageSize(dto == null ? null : dto.getPageSize()); HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().startedBy(currentUserId());
        if ("PENDING".equals(status)) query.unfinished(); else if ("TERMINATED".equals(status)) query.deleted(); else if ("APPROVED".equals(status)) query.finished().variableValueEquals("approved", true); else if ("REJECTED".equals(status)) query.finished().variableValueEquals("approved", false); else if (!status.isEmpty()) throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID, "不支持的流程状态");
        query.orderByProcessInstanceStartTime().desc(); List<HistoricProcessInstance> items = query.listPage((int) ((page - 1) * size), (int) size);
        return new PageResponse<>(query.count(), page, size, items.stream().map(item -> new ProcessInstanceVO(item.getId(), item.getProcessDefinitionKey(), item.getProcessDefinitionName(), item.getBusinessKey(), statusOf(item), item.getStartUserId(), startUserName(item), currentTaskName(item), item.getStartTime(), item.getEndTime())).toList());
    }

    @Override
    public WorkflowHomeSummaryVO getHomeSummary() {
        String userId = currentUserId();
        WorkflowHomeSummaryVO summary = new WorkflowHomeSummaryVO();
        summary.setTodoCount(String.valueOf(countTodo(userId)));
        summary.setPendingReviewCount(String.valueOf(countPendingReview(userId)));
        summary.setReviewedCount(String.valueOf(countReviewed(userId)));
        summary.setRejectedCount(String.valueOf(countRejected(userId)));
        summary.setTrend(listRecentThreeDayTrend(userId));
        return summary;
    }

    @Override
    public String startProcess(ProcessStartDTO dto) {
        if (dto == null) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID);
        }
        String processKey = requireKey(dto.getProcessKey());
        String businessKey = requireKey(dto.getBusinessKey());
        String startUserId = resolveStartUserId(dto);
        try {
            if (startUserId != null) {
                identityService.setAuthenticatedUserId(startUserId);
            }
            Map<String, Object> variables = dto.getVariables() == null ? new HashMap<>() : new HashMap<>(dto.getVariables());
            CurrentLoginUser currentUser = UserContextHolder.getCurrentUser();
            if (currentUser != null) {
                variables.putIfAbsent("startUserId", String.valueOf(currentUser.getUserId()));
                String name = currentUser.getNickname() == null || currentUser.getNickname().isBlank() ? currentUser.getUsername() : currentUser.getNickname();
                if (name != null && !name.isBlank()) variables.putIfAbsent("startUserName", name);
            }
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
            return instance.getId();
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    public void completeTask(TaskCompleteDTO dto) {
        if (dto == null) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID);
        }
        String taskId = requireKey(dto.getTaskId());
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException(WorkflowErrorCode.TASK_NOT_FOUND);
        }
        String comment = normalizeOptionalText(dto.getComment(), MAX_REASON_LENGTH);
        if (comment != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }
        taskService.complete(taskId, dto.getVariables());
    }

    @Override
    public void terminateProcess(ProcessTerminateDTO dto) {
        if (dto == null) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID);
        }
        String businessKey = requireKey(dto.getBusinessKey());
        String reason = normalizeOptionalText(dto.getReason(), MAX_REASON_LENGTH);
        if (reason == null) {
            reason = "流程已终止";
        }
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .singleResult();
        if (instance == null) {
            throw new BizException(WorkflowErrorCode.PROCESS_INSTANCE_NOT_FOUND);
        }
        runtimeService.deleteProcessInstance(instance.getId(), reason);
        workflowEventService.publishStatusChange(businessKey, TERMINATED_STATUS, reason);
    }

    private Map<String, ProcessInstance> listRuntimeInstanceMap(List<Task> tasks) {
        Set<String> processInstanceIds = tasks.stream()
                .map(Task::getProcessInstanceId)
                .collect(Collectors.toSet());
        if (processInstanceIds.isEmpty()) {
            return Map.of();
        }
        return runtimeService.createProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .list()
                .stream()
                .collect(Collectors.toMap(ProcessInstance::getId, Function.identity()));
    }

    private List<WorkflowDailyCountVO> listRecentThreeDayTrend(String userId) {
        List<WorkflowDailyCountVO> trend = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(6);
        for (int i = 0; i < 6; i++) {
            LocalDate date = startDate.plusDays(i);
            Date startTime = toDate(date);
            Date endTime = toDate(date.plusDays(1));
            trend.add(new WorkflowDailyCountVO(
                    date.toString(),
                    String.valueOf(countTodo(userId, startTime, endTime)),
                    String.valueOf(countPendingReview(userId, startTime, endTime)),
                    String.valueOf(countReviewed(userId, startTime, endTime)),
                    String.valueOf(countRejected(userId, startTime, endTime))));
        }
        return trend;
    }

    private long countTodo(String userId) {
        return taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .count();
    }

    private long countTodo(String userId, Date startTime, Date endTime) {
        return taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .taskCreatedAfter(startTime)
                .taskCreatedBefore(endTime)
                .count();
    }

    private long countPendingReview(String userId) {
        return historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .startedBy(userId)
                .unfinished()
                .count();
    }

    private long countPendingReview(String userId, Date startTime, Date endTime) {
        return historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .startedBy(userId)
                .unfinished()
                .startedAfter(startTime)
                .startedBefore(endTime)
                .count();
    }

    private long countReviewed(String userId) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .count();
    }

    private long countReviewed(String userId, Date startTime, Date endTime) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .taskCompletedAfter(startTime)
                .taskCompletedBefore(endTime)
                .count();
    }

    private long countRejected(String userId) {
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .deleted()
                .count();
    }

    private long countRejected(String userId, Date startTime, Date endTime) {
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .deleted()
                .finishedAfter(startTime)
                .finishedBefore(endTime)
                .count();
    }

    private Map<String, HistoricProcessInstance> listHistoricInstanceMap(List<HistoricTaskInstance> tasks) {
        Set<String> processInstanceIds = tasks.stream()
                .map(HistoricTaskInstance::getProcessInstanceId)
                .collect(Collectors.toSet());
        if (processInstanceIds.isEmpty()) {
            return Map.of();
        }
        return listHistoricInstanceMapByIds(processInstanceIds);
    }

    private Map<String, HistoricProcessInstance> listHistoricInstanceMapByIds(Set<String> processInstanceIds) {
        if (processInstanceIds.isEmpty()) {
            return Map.of();
        }
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricProcessInstance::getId, Function.identity()));
    }

    private TaskVO toTaskVO(Task task, ProcessInstance instance, HistoricProcessInstance historicInstance) {
        String businessKey = instance == null ? null : instance.getBusinessKey();
        TaskVO vo = new TaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setBusinessKey(businessKey);
        vo.setCreateTime(task.getCreateTime());
        vo.setStatus("TODO");
        vo.setProcessDefinitionName(instance == null ? null : instance.getProcessDefinitionName());
        vo.setStartUserId(historicInstance == null ? null : historicInstance.getStartUserId());
        vo.setStartUserName(historicInstance == null ? null : startUserName(historicInstance));
        vo.setStartTime(historicInstance == null ? null : historicInstance.getStartTime());
        vo.setCurrentTaskName(task.getName());
        return vo;
    }

    private String historicBusinessKey(HistoricTaskInstance task, Map<String, HistoricProcessInstance> instanceMap) {
        HistoricProcessInstance instance = instanceMap.get(task.getProcessInstanceId());
        if (instance == null) {
            return null;
        }
        return instance.getBusinessKey();
    }

    private String requireKey(String value) {
        String normalized = requireText(value);
        if (normalized.length() > MAX_KEY_LENGTH) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID);
        }
        return normalized;
    }
    private long pageNum(Integer value) { return value == null || value < 1 ? 1 : value; }
    private long pageSize(Integer value) { return value == null || value < 1 ? 10 : Math.min(value, maxQuerySize); }
    private String statusOf(HistoricProcessInstance item) { return item.getEndTime() == null ? "PENDING" : item.getDeleteReason() != null ? "TERMINATED" : "FINISHED"; }
    private String currentTaskName(HistoricProcessInstance item) { if (item.getEndTime() != null) return null; List<Task> tasks = taskService.createTaskQuery().processInstanceId(item.getId()).orderByTaskCreateTime().asc().listPage(0, 1); return tasks.isEmpty() ? null : tasks.get(0).getName(); }
    private String startUserName(HistoricProcessInstance item) { Object value = item.getProcessVariables() == null ? null : item.getProcessVariables().get("startUserName"); return value == null ? item.getStartUserId() : String.valueOf(value); }

    private String currentUserId() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID, "未获取到当前登录用户");
        }
        return String.valueOf(userId);
    }

    private String resolveStartUserId(ProcessStartDTO dto) {
        Long currentUserId = UserContextHolder.getUserId();
        if (currentUserId != null) {
            return String.valueOf(currentUserId);
        }
        if (dto.getVariables() == null) {
            return null;
        }
        Object startUserId = dto.getVariables().get("startUserId");
        if (startUserId == null) {
            startUserId = dto.getVariables().get("userId");
        }
        if (startUserId == null) {
            return null;
        }
        String value = String.valueOf(startUserId);
        if (value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BizException(WorkflowErrorCode.BIZ_PARAM_INVALID);
        }
        return normalized;
    }
}
