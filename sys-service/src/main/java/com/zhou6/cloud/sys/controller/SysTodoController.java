package com.zhou6.cloud.sys.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.IdDTO;
import com.zhou6.cloud.sys.dto.TodoPageQueryDTO;
import com.zhou6.cloud.sys.dto.TodoReminderReadDTO;
import com.zhou6.cloud.sys.dto.TodoSaveDTO;
import com.zhou6.cloud.sys.service.SysTodoService;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.TodoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "我的待办", description = "管理用户手工创建的日期待办，并提供到期后的站内提醒")
@RestController
@RequestMapping(SysApiPathConstants.TODO)
public class SysTodoController {

    private final SysTodoService todoService;

    public SysTodoController(SysTodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping("/add")
    @Operation(summary = "新增手工待办")
    public R<Void> add(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody TodoSaveDTO dto) {
        todoService.addManual(currentUserId(userId), dto);
        return R.ok(null);
    }

    @PostMapping("/edit")
    @Operation(summary = "修改手工待办")
    public R<Void> edit(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody TodoSaveDTO dto) {
        todoService.editManual(currentUserId(userId), dto);
        return R.ok(null);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成手工待办")
    public R<Void> complete(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody IdDTO dto) {
        todoService.completeManual(currentUserId(userId), dto == null ? null : dto.getId());
        return R.ok(null);
    }

    @PostMapping({"/cancel", "/delete"})
    @Operation(summary = "取消手工待办", description = "delete 为兼容入口，实际执行取消并保留历史")
    public R<Void> cancel(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody IdDTO dto) {
        todoService.cancelManual(currentUserId(userId), dto == null ? null : dto.getId());
        return R.ok(null);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询我的待办")
    public R<PageResponse<TodoVO>> page(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody(required = false) TodoPageQueryDTO dto) {
        return R.ok(todoService.page(currentUserId(userId), dto));
    }

    @PostMapping("/calendar")
    @Operation(summary = "按日期范围查询我的待办")
    public R<List<TodoVO>> calendar(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody TodoPageQueryDTO dto) {
        return R.ok(todoService.calendar(currentUserId(userId), dto));
    }

    @PostMapping("/reminder/unread")
    @Operation(summary = "查询当前用户未读提醒")
    public R<List<TodoVO>> unreadReminders(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(todoService.unreadReminders(currentUserId(userId)));
    }

    @PostMapping("/reminder/read")
    @Operation(summary = "标记提醒已读")
    public R<Void> markRemindersRead(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody TodoReminderReadDTO dto) {
        todoService.markRemindersRead(currentUserId(userId), dto);
        return R.ok(null);
    }

    @PostMapping("/reminder/unreadCount")
    @Operation(summary = "查询当前用户未读提醒数量")
    public R<String> unreadReminderCount(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(String.valueOf(todoService.unreadReminderCount(currentUserId(userId))));
    }

    private Long currentUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "缺少当前用户信息");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "当前用户信息不正确");
        }
    }
}
