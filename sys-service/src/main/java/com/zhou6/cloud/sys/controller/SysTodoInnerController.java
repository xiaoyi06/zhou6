package com.zhou6.cloud.sys.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.sys.constant.SysApiPathConstants;
import com.zhou6.cloud.sys.dto.WorkflowTodoStatusDTO;
import com.zhou6.cloud.sys.dto.WorkflowTodoUpsertDTO;
import com.zhou6.cloud.sys.service.SysTodoService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 供工作流等可信内部服务调用，不能直接暴露给浏览器前端。 */
@Hidden
@RestController
@RequestMapping(SysApiPathConstants.TODO_INNER)
public class SysTodoInnerController {

    private final SysTodoService todoService;

    public SysTodoInnerController(SysTodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping("/upsertWorkflowTodo")
    public R<Void> upsertWorkflowTodo(@RequestBody WorkflowTodoUpsertDTO dto) {
        todoService.upsertWorkflowTodo(dto);
        return R.ok(null);
    }

    @PostMapping("/completeBySource")
    public R<Void> completeBySource(@RequestBody WorkflowTodoStatusDTO dto) {
        todoService.completeWorkflowTodo(dto);
        return R.ok(null);
    }

    @PostMapping("/cancelBySource")
    public R<Void> cancelBySource(@RequestBody WorkflowTodoStatusDTO dto) {
        todoService.cancelWorkflowTodo(dto);
        return R.ok(null);
    }
}
