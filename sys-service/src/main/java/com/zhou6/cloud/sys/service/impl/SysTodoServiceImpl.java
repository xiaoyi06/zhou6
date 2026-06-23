package com.zhou6.cloud.sys.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.sys.dto.TodoPageQueryDTO;
import com.zhou6.cloud.sys.dto.TodoReminderReadDTO;
import com.zhou6.cloud.sys.dto.TodoSaveDTO;
import com.zhou6.cloud.sys.dto.WorkflowTodoStatusDTO;
import com.zhou6.cloud.sys.dto.WorkflowTodoUpsertDTO;
import com.zhou6.cloud.sys.entity.SysTodo;
import com.zhou6.cloud.sys.mapper.SysTodoMapper;
import com.zhou6.cloud.sys.service.SysTodoService;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.TodoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysTodoServiceImpl extends BaseSysService implements SysTodoService {

    private static final String STATUS_TODO = "TODO";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String REMIND_PENDING = "PENDING";
    private static final String REMIND_SENT = "SENT";
    private static final String SOURCE_MANUAL = "MANUAL";
    private static final String SOURCE_WORKFLOW = "WORKFLOW";
    private static final String COMPLETE_USER = "USER";
    private static final String COMPLETE_WORKFLOW = "WORKFLOW";
    private static final int REMINDER_BATCH_SIZE = 100;

    private final SysTodoMapper todoMapper;

    public SysTodoServiceImpl(SysTodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    public void addManual(Long userId, TodoSaveDTO dto) {
        requireCurrentUser(userId);
        validateManualSave(dto, false);
        SysTodo entity = new SysTodo();
        entity.setUserId(userId);
        fillManual(entity, dto);
        entity.setStatus(STATUS_TODO);
        entity.setRemindStatus(REMIND_PENDING);
        entity.setSourceType(SOURCE_MANUAL);
        entity.setCompleteMode(COMPLETE_USER);
        todoMapper.insert(entity);
    }

    @Override
    public void editManual(Long userId, TodoSaveDTO dto) {
        requireCurrentUser(userId);
        validateManualSave(dto, true);
        Long id = parseRequiredId(dto.getId(), "待办ID不能为空");
        requireManualTodo(userId, id);
        todoMapper.update(null, new LambdaUpdateWrapper<SysTodo>()
                .eq(SysTodo::getId, id)
                .set(SysTodo::getTitle, dto.getTitle().trim())
                .set(SysTodo::getContent, hasText(dto.getContent()) ? dto.getContent().trim() : null)
                .set(SysTodo::getRemindTime, dto.getRemindTime())
                .set(SysTodo::getRemindStatus, REMIND_PENDING)
                .set(SysTodo::getNotifiedAt, null)
                .set(SysTodo::getReadAt, null));
    }

    @Override
    public void completeManual(Long userId, String id) {
        updateManualStatus(userId, id, STATUS_DONE);
    }

    @Override
    public void cancelManual(Long userId, String id) {
        updateManualStatus(userId, id, STATUS_CANCELLED);
    }

    @Override
    public PageResponse<TodoVO> page(Long userId, TodoPageQueryDTO dto) {
        requireCurrentUser(userId);
        TodoPageQueryDTO query = dto == null ? new TodoPageQueryDTO() : dto;
        validateQuery(query);
        Page<SysTodo> page = todoMapper.selectPage(Page.of(pageNum(query.getPageNum()), pageSize(query.getPageSize())),
                baseQuery(userId, query).orderByAsc(SysTodo::getRemindTime).orderByDesc(SysTodo::getId));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toVo).toList());
    }

    @Override
    public List<TodoVO> calendar(Long userId, TodoPageQueryDTO dto) {
        requireCurrentUser(userId);
        TodoPageQueryDTO query = dto == null ? new TodoPageQueryDTO() : dto;
        validateQuery(query);
        require(query.getBeginTime() != null && query.getEndTime() != null, "日历查询必须指定开始和结束时间");
        return todoMapper.selectList(baseQuery(userId, query)
                .orderByAsc(SysTodo::getRemindTime).orderByDesc(SysTodo::getId))
                .stream().map(this::toVo).toList();
    }

    @Override
    public List<TodoVO> unreadReminders(Long userId) {
        requireCurrentUser(userId);
        return todoMapper.selectList(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, STATUS_TODO)
                .eq(SysTodo::getRemindStatus, REMIND_SENT)
                .orderByAsc(SysTodo::getNotifiedAt)
                .orderByAsc(SysTodo::getId))
                .stream().map(this::toVo).toList();
    }

    @Override
    public long unreadReminderCount(Long userId) {
        requireCurrentUser(userId);
        Long count = todoMapper.selectCount(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, STATUS_TODO)
                .eq(SysTodo::getRemindStatus, REMIND_SENT));
        return count == null ? 0L : count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRemindersRead(Long userId, TodoReminderReadDTO dto) {
        requireCurrentUser(userId);
        require(dto != null && dto.getIds() != null && !dto.getIds().isEmpty(), "提醒ID不能为空");
        List<Long> ids = dto.getIds().stream()
                .map(id -> parseRequiredId(id, "提醒ID不正确"))
                .distinct()
                .toList();
        todoMapper.markRemindersRead(userId, ids, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertWorkflowTodo(WorkflowTodoUpsertDTO dto) {
        require(dto != null, "流程待办参数不能为空");
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        validateWorkflowTodo(dto);
        SysTodo existing = todoMapper.selectOne(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getSourceType, SOURCE_WORKFLOW)
                .eq(SysTodo::getSourceId, dto.getSourceId().trim())
                .last("limit 1"));
        if (existing == null) {
            SysTodo entity = new SysTodo();
            entity.setUserId(userId);
            entity.setStatus(STATUS_TODO);
            entity.setRemindStatus(REMIND_PENDING);
            entity.setSourceType(SOURCE_WORKFLOW);
            entity.setCompleteMode(COMPLETE_WORKFLOW);
            fillWorkflow(entity, dto);
            todoMapper.insert(entity);
            return;
        }
        if (!STATUS_TODO.equals(existing.getStatus())) {
            return;
        }
        todoMapper.update(null, new LambdaUpdateWrapper<SysTodo>()
                .eq(SysTodo::getId, existing.getId())
                .set(SysTodo::getTitle, dto.getTitle().trim())
                .set(SysTodo::getContent, hasText(dto.getContent()) ? dto.getContent().trim() : null)
                .set(SysTodo::getRemindTime, dto.getRemindTime())
                .set(SysTodo::getSourceBusinessType,
                        hasText(dto.getSourceBusinessType()) ? dto.getSourceBusinessType().trim() : null)
                .set(SysTodo::getSourceBusinessId,
                        hasText(dto.getSourceBusinessId()) ? dto.getSourceBusinessId().trim() : null)
                .set(SysTodo::getRemindStatus, REMIND_PENDING)
                .set(SysTodo::getNotifiedAt, null)
                .set(SysTodo::getReadAt, null));
    }

    @Override
    public void completeWorkflowTodo(WorkflowTodoStatusDTO dto) {
        updateWorkflowStatus(dto, STATUS_DONE);
    }

    @Override
    public void cancelWorkflowTodo(WorkflowTodoStatusDTO dto) {
        updateWorkflowStatus(dto, STATUS_CANCELLED);
    }

    @Override
    public void dispatchDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<SysTodo> dueTodos = todoMapper.selectList(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getStatus, STATUS_TODO)
                .eq(SysTodo::getRemindStatus, REMIND_PENDING)
                .isNotNull(SysTodo::getRemindTime)
                .le(SysTodo::getRemindTime, now)
                .orderByAsc(SysTodo::getRemindTime)
                .last("limit " + REMINDER_BATCH_SIZE));
        for (SysTodo todo : dueTodos) {
            todoMapper.claimReminder(todo.getId(), now);
        }
    }

    private void updateManualStatus(Long userId, String id, String targetStatus) {
        requireCurrentUser(userId);
        Long todoId = parseRequiredId(id, "待办ID不能为空");
        requireManualTodo(userId, todoId);
        todoMapper.update(null, new LambdaUpdateWrapper<SysTodo>()
                .eq(SysTodo::getId, todoId)
                .set(SysTodo::getStatus, targetStatus));
    }

    private void updateWorkflowStatus(WorkflowTodoStatusDTO dto, String targetStatus) {
        require(dto != null, "流程待办参数不能为空");
        Long userId = parseRequiredId(dto.getUserId(), "用户ID不能为空");
        require(hasText(dto.getSourceId()), "流程来源ID不能为空");
        todoMapper.update(null, new LambdaUpdateWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getSourceType, SOURCE_WORKFLOW)
                .eq(SysTodo::getSourceId, dto.getSourceId().trim())
                .eq(SysTodo::getStatus, STATUS_TODO)
                .set(SysTodo::getStatus, targetStatus));
    }

    private SysTodo requireManualTodo(Long userId, Long id) {
        SysTodo todo = todoMapper.selectById(id);
        require(todo != null, "待办不存在");
        require(userId.equals(todo.getUserId()), "无权操作该待办");
        require(SOURCE_MANUAL.equals(todo.getSourceType()), "流程待办不能由用户直接操作");
        require(STATUS_TODO.equals(todo.getStatus()), "待办已结束，不能再操作");
        return todo;
    }

    private LambdaQueryWrapper<SysTodo> baseQuery(Long userId, TodoPageQueryDTO query) {
        return new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(hasText(query.getStatus()), SysTodo::getStatus, normalizeStatus(query.getStatus()))
                .ge(query.getBeginTime() != null, SysTodo::getRemindTime, query.getBeginTime())
                .le(query.getEndTime() != null, SysTodo::getRemindTime, query.getEndTime());
    }

    private void validateManualSave(TodoSaveDTO dto, boolean editing) {
        require(dto != null, "待办参数不能为空");
        if (editing) {
            require(hasText(dto.getId()), "待办ID不能为空");
        }
        require(hasText(dto.getTitle()), "待办标题不能为空");
        require(dto.getTitle().trim().length() <= 200, "待办标题不能超过200个字符");
        require(dto.getContent() == null || dto.getContent().trim().length() <= 2000, "待办内容不能超过2000个字符");
        require(dto.getRemindTime() != null, "提醒时间不能为空");
    }

    private void validateWorkflowTodo(WorkflowTodoUpsertDTO dto) {
        require(hasText(dto.getTitle()), "待办标题不能为空");
        require(dto.getTitle().trim().length() <= 200, "待办标题不能超过200个字符");
        require(dto.getContent() == null || dto.getContent().trim().length() <= 2000, "待办内容不能超过2000个字符");
        require(dto.getRemindTime() != null, "提醒时间不能为空");
        require(hasText(dto.getSourceId()), "流程来源ID不能为空");
    }

    private void validateQuery(TodoPageQueryDTO query) {
        if (hasText(query.getStatus())) {
            normalizeStatus(query.getStatus());
        }
        require(query.getBeginTime() == null || query.getEndTime() == null
                || !query.getBeginTime().isAfter(query.getEndTime()), "开始时间不能晚于结束时间");
    }

    private String normalizeStatus(String status) {
        String value = status == null ? null : status.trim().toUpperCase();
        require(STATUS_TODO.equals(value) || STATUS_DONE.equals(value) || STATUS_CANCELLED.equals(value), "待办状态不正确");
        return value;
    }

    private void fillManual(SysTodo entity, TodoSaveDTO dto) {
        entity.setTitle(dto.getTitle().trim());
        entity.setContent(hasText(dto.getContent()) ? dto.getContent().trim() : null);
        entity.setRemindTime(dto.getRemindTime());
    }

    private void fillWorkflow(SysTodo entity, WorkflowTodoUpsertDTO dto) {
        entity.setTitle(dto.getTitle().trim());
        entity.setContent(hasText(dto.getContent()) ? dto.getContent().trim() : null);
        entity.setRemindTime(dto.getRemindTime());
        entity.setSourceId(dto.getSourceId().trim());
        entity.setSourceBusinessType(hasText(dto.getSourceBusinessType()) ? dto.getSourceBusinessType().trim() : null);
        entity.setSourceBusinessId(hasText(dto.getSourceBusinessId()) ? dto.getSourceBusinessId().trim() : null);
    }

    private TodoVO toVo(SysTodo entity) {
        TodoVO vo = new TodoVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setRemindTime(entity.getRemindTime());
        vo.setStatus(entity.getStatus());
        vo.setRemindStatus(entity.getRemindStatus());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceId(entity.getSourceId());
        vo.setSourceBusinessType(entity.getSourceBusinessType());
        vo.setSourceBusinessId(entity.getSourceBusinessId());
        vo.setCompleteMode(entity.getCompleteMode());
        vo.setNotifiedAt(entity.getNotifiedAt());
        vo.setReadAt(entity.getReadAt());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private void requireCurrentUser(Long userId) {
        require(userId != null && userId > 0, "当前用户不存在");
    }
}
