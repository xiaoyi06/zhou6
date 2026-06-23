package com.zhou6.cloud.workflow.service;

import java.util.List;

import com.zhou6.cloud.workflow.dto.ProcessStartDTO;
import com.zhou6.cloud.workflow.dto.ProcessTerminateDTO;
import com.zhou6.cloud.workflow.dto.TaskCompleteDTO;
import com.zhou6.cloud.workflow.dto.TaskPageQueryDTO;
import com.zhou6.cloud.workflow.dto.InitiatedProcessPageQueryDTO;
import com.zhou6.cloud.workflow.vo.HistoricTaskVO;
import com.zhou6.cloud.workflow.vo.TaskVO;
import com.zhou6.cloud.workflow.vo.WorkflowHomeSummaryVO;
import com.zhou6.cloud.workflow.vo.PageResponse;
import com.zhou6.cloud.workflow.vo.ProcessInstanceVO;

/**
 * 工作流流程操作服务。
 */
public interface WorkflowProcessService {

    /**
     * 查询当前用户待办任务。
     *
     * @param userId 用户 ID
     * @return 待办任务列表
     */
    List<TaskVO> listTodoTasks(String userId);

    /**
     * 查询当前用户已办任务。
     *
     * @param userId 用户 ID
     * @return 已办任务列表
     */
    List<HistoricTaskVO> listDoneTasks(String userId);
    PageResponse<TaskVO> pageTodoTasks(TaskPageQueryDTO dto);
    PageResponse<HistoricTaskVO> pageDoneTasks(TaskPageQueryDTO dto);
    PageResponse<ProcessInstanceVO> pageInitiatedProcesses(InitiatedProcessPageQueryDTO dto);

    /**
     * 查询当前用户首页流程统计。
     *
     * @return 首页统计数据
     */
    WorkflowHomeSummaryVO getHomeSummary();

    /**
     * 启动流程实例。
     *
     * @param dto 流程启动参数
     * @return 流程实例 ID
     */
    String startProcess(ProcessStartDTO dto);

    /**
     * 办理流程任务。
     *
     * @param dto 任务办理参数
     */
    void completeTask(TaskCompleteDTO dto);

    /**
     * 终止流程实例。
     *
     * @param dto 流程终止参数
     */
    void terminateProcess(ProcessTerminateDTO dto);
}
