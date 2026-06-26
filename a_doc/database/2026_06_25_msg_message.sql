CREATE TABLE IF NOT EXISTS msg_message (
    id BIGINT PRIMARY KEY,
    channel VARCHAR(20) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000),
    source_type VARCHAR(50),
    source_name VARCHAR(100),
    source_id VARCHAR(100),
    business_type VARCHAR(50),
    business_id VARCHAR(100),
    link_type VARCHAR(50) NOT NULL DEFAULT 'NONE',
    link_url VARCHAR(500),
    link_params TEXT,
    send_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_msg_message_channel CHECK (channel IN ('INTERNAL', 'EXTERNAL'))
);

CREATE TABLE IF NOT EXISTS msg_message_receiver (
    id BIGINT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    read_time TIMESTAMP,
    push_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    push_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_msg_message_receiver_read CHECK (read_status IN ('UNREAD', 'READ')),
    CONSTRAINT ck_msg_message_receiver_push CHECK (push_status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT fk_msg_message_receiver_message
        FOREIGN KEY (message_id) REFERENCES msg_message(id)
);

CREATE INDEX IF NOT EXISTS idx_msg_message_send_time
    ON msg_message (send_time DESC);
CREATE INDEX IF NOT EXISTS idx_msg_message_channel_type_time
    ON msg_message (channel, message_type, send_time DESC);
CREATE INDEX IF NOT EXISTS idx_msg_message_receiver_user_read
    ON msg_message_receiver (user_id, read_status);
CREATE INDEX IF NOT EXISTS idx_msg_message_receiver_message
    ON msg_message_receiver (message_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_msg_message_receiver_user
    ON msg_message_receiver (message_id, user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_msg_message_source
    ON msg_message (channel, source_type, source_id)
    WHERE source_type IS NOT NULL AND source_id IS NOT NULL;

COMMENT ON TABLE msg_message IS '消息中心消息主体表，保存站内/站外通知公共内容';
COMMENT ON TABLE msg_message_receiver IS '消息中心接收人表，保存每个用户的已读和推送状态';
COMMENT ON COLUMN msg_message.channel IS '消息通道：INTERNAL 站内，EXTERNAL 站外';
COMMENT ON COLUMN msg_message.message_type IS '消息类型：APPROVAL、WORKFLOW、TODO、PERMISSION、ACCOUNT、PASSWORD、EMAIL、API、ALERT、SYSTEM 等';
COMMENT ON COLUMN msg_message.source_type IS '触发来源类型：WORKFLOW、TODO、PERMISSION、ACCOUNT、PASSWORD、EXTERNAL_SYSTEM、GATEWAY、MONITOR 等';
COMMENT ON COLUMN msg_message.source_id IS '来源唯一标识；与 channel/source_type 组合用于消息幂等';
COMMENT ON COLUMN msg_message.link_type IS '跳转类型：ROUTE、URL、NONE';
COMMENT ON COLUMN msg_message_receiver.id IS '接收人消息ID，前端已读接口传该ID';
COMMENT ON COLUMN msg_message_receiver.read_status IS '阅读状态：UNREAD、READ';
COMMENT ON COLUMN msg_message_receiver.push_status IS 'WebSocket 推送状态：PENDING、SENT、FAILED';
