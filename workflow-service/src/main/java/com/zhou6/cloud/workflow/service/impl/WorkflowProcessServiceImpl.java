package com.zhou6.cloud.workflow.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.workflow.dto.ProcessStartDTO;
import com.zhou6.cloud.workflow.dto.ProcessTerminateDTO;
import com.zhou6.cloud.workflow.dto.TaskCompleteDTO;
import com.zhou6.cloud.workflow.handler.WorkflowErrorCode;
import com.zhou6.cloud.workflow.service.WorkflowEventService;
import com.zhou6.cloud.workflow.service.WorkflowProcessService;
import com.zhou6.cloud.workflow.vo.HistoricTaskVO;
import com.zhou6.cloud.workflow.vo.TaskVO;
import com.zhou6.cloud.workflow.vo.WorkflowDailyCountVO;
import com.zhou6.cloud.workflow.vo.WorkflowHomeSummaryVO;
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
                .map(task -> toTaskVO(task, instanceMap.get(task.getProcessInstanceId())))
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
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, businessKey,
                    dto.getVariables());
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
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .unfinished()
                .count();
    }

    private long countPendingReview(String userId, Date startTime, Date endTime) {
        return historyService.createHistoricProcessInstanceQuery()
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
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricProcessInstance::getId, Function.identity()));
    }

    private TaskVO toTaskVO(Task task, ProcessInstance instance) {
        String businessKey = instance == null ? null : instance.getBusinessKey();
        return new TaskVO(task.getId(), task.getName(), task.getProcessInstanceId(), businessKey, task.getCreateTime());
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
