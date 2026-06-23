CREATE SCHEMA IF NOT EXISTS zhou6;
SET search_path TO zhou6;

CREATE TABLE IF NOT EXISTS sys_todo (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000),
    remind_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    remind_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notified_at TIMESTAMP,
    read_at TIMESTAMP,
    source_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    source_id VARCHAR(100),
    source_business_type VARCHAR(100),
    source_business_id VARCHAR(100),
    complete_mode VARCHAR(20) NOT NULL DEFAULT 'USER',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_sys_todo_status CHECK (status IN ('TODO', 'DONE', 'CANCELLED')),
    CONSTRAINT ck_sys_todo_remind_status CHECK (remind_status IN ('PENDING', 'SENT', 'READ')),
    CONSTRAINT ck_sys_todo_source_type CHECK (source_type IN ('MANUAL', 'WORKFLOW')),
    CONSTRAINT ck_sys_todo_complete_mode CHECK (complete_mode IN ('USER', 'WORKFLOW')),
    CONSTRAINT ck_sys_todo_workflow_source CHECK (
        (source_type = 'MANUAL' AND source_id IS NULL AND complete_mode = 'USER')
        OR (source_type = 'WORKFLOW' AND source_id IS NOT NULL AND complete_mode = 'WORKFLOW')
    )
);

CREATE INDEX IF NOT EXISTS idx_sys_todo_due
    ON sys_todo (remind_time, remind_status, status);
CREATE INDEX IF NOT EXISTS idx_sys_todo_user_status_time
    ON sys_todo (user_id, status, remind_time);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_todo_workflow_source_user
    ON sys_todo (source_type, source_id, user_id)
    WHERE source_type = 'WORKFLOW' AND source_id IS NOT NULL;

COMMENT ON TABLE sys_todo IS '统一待办表，包含用户手工待办和流程生成待办';
COMMENT ON COLUMN sys_todo.user_id IS '待办所属用户ID';
COMMENT ON COLUMN sys_todo.remind_time IS '单次站内提醒触发时间';
COMMENT ON COLUMN sys_todo.status IS '待办状态：TODO、DONE、CANCELLED';
COMMENT ON COLUMN sys_todo.remind_status IS '提醒状态：PENDING、SENT、READ';
COMMENT ON COLUMN sys_todo.source_type IS '待办来源：MANUAL、WORKFLOW';
COMMENT ON COLUMN sys_todo.source_id IS '外部来源唯一标识；流程待办使用流程任务ID';
COMMENT ON COLUMN sys_todo.complete_mode IS '完成方：USER、WORKFLOW';
