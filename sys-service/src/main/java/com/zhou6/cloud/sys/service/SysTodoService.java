package com.zhou6.cloud.sys.service;

import java.util.List;

import com.zhou6.cloud.sys.dto.TodoPageQueryDTO;
import com.zhou6.cloud.sys.dto.TodoReminderReadDTO;
import com.zhou6.cloud.sys.dto.TodoSaveDTO;
import com.zhou6.cloud.sys.dto.WorkflowTodoStatusDTO;
import com.zhou6.cloud.sys.dto.WorkflowTodoUpsertDTO;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.TodoVO;

/** 统一待办服务。 */
public interface SysTodoService {

    void addManual(Long userId, TodoSaveDTO dto);

    void editManual(Long userId, TodoSaveDTO dto);

    void completeManual(Long userId, String id);

    void cancelManual(Long userId, String id);

    PageResponse<TodoVO> page(Long userId, TodoPageQueryDTO dto);

    List<TodoVO> calendar(Long userId, TodoPageQueryDTO dto);

    List<TodoVO> unreadReminders(Long userId);

    long unreadReminderCount(Long userId);

    void markRemindersRead(Long userId, TodoReminderReadDTO dto);

    void upsertWorkflowTodo(WorkflowTodoUpsertDTO dto);

    void completeWorkflowTodo(WorkflowTodoStatusDTO dto);

    void cancelWorkflowTodo(WorkflowTodoStatusDTO dto);

    void dispatchDueReminders();
}
