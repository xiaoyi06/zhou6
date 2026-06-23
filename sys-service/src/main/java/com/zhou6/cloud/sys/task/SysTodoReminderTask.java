package com.zhou6.cloud.sys.task;

import com.zhou6.cloud.sys.service.SysTodoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 扫描到期个人与流程待办，并把它们转换为待前端确认的站内提醒。 */
@Component
public class SysTodoReminderTask {

    private final SysTodoService todoService;

    public SysTodoReminderTask(SysTodoService todoService) {
        this.todoService = todoService;
    }

    @Scheduled(fixedDelayString = "${zhou6.sys.todo.reminder-scan-delay-ms:30000}")
    public void dispatchDueReminders() {
        todoService.dispatchDueReminders();
    }
}
