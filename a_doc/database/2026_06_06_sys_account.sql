CREATE SCHEMA IF NOT EXISTS zhou6;

CREATE TABLE IF NOT EXISTS zhou6.sys_account (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    available_amount NUMERIC(16, 4) NOT NULL DEFAULT 0.0000,
    frozen_amount NUMERIC(16, 4) NOT NULL DEFAULT 0.0000,
    settling_amount NUMERIC(16, 4) NOT NULL DEFAULT 0.0000,
    total_amount NUMERIC(16, 4) NOT NULL DEFAULT 0.0000,
    version INT NOT NULL DEFAULT 0,
    create_by BIGINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL DEFAULT 0,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_id UNIQUE (user_id)
);

COMMENT ON TABLE zhou6.sys_account IS '用户现金余额账户主表';
COMMENT ON COLUMN zhou6.sys_account.id IS '雪花算法主键ID';
COMMENT ON COLUMN zhou6.sys_account.user_id IS '统一用户中心用户ID';
COMMENT ON COLUMN zhou6.sys_account.available_amount IS '用户当前完全可支配的可用现金余额';
COMMENT ON COLUMN zhou6.sys_account.frozen_amount IS '提现中、或订单待支付中被锁定的冻结现金';
COMMENT ON COLUMN zhou6.sys_account.settling_amount IS '已获得但处于在途、未到账的待结算现金';
COMMENT ON COLUMN zhou6.sys_account.total_amount IS '该账户历史累计获得的现金总额，只增不减';
COMMENT ON COLUMN zhou6.sys_account.version IS '乐观锁版本号，用于DB级并发控制';
COMMENT ON COLUMN zhou6.sys_account.create_by IS '创建人用户ID，0代表系统自动初始化';
COMMENT ON COLUMN zhou6.sys_account.create_time IS '账户开户时间';
COMMENT ON COLUMN zhou6.sys_account.update_by IS '最后修改人用户ID';
COMMENT ON COLUMN zhou6.sys_account.update_time IS '大账最后更新时间';

CREATE TABLE IF NOT EXISTS zhou6.sys_account_flow (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    direction SMALLINT NOT NULL,
    target_type SMALLINT NOT NULL,
    amount NUMERIC(16, 4) NOT NULL,
    biz_type VARCHAR(50) NOT NULL,
    biz_id VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    create_by BIGINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_flow UNIQUE (biz_type, biz_id)
);

COMMENT ON TABLE zhou6.sys_account_flow IS '现金变动流水历史表，禁删改';
COMMENT ON COLUMN zhou6.sys_account_flow.id IS '流水号主键，雪花ID';
COMMENT ON COLUMN zhou6.sys_account_flow.user_id IS '统一用户中心用户ID';
COMMENT ON COLUMN zhou6.sys_account_flow.direction IS '流水方向：1-入账，2-出账';
COMMENT ON COLUMN zhou6.sys_account_flow.target_type IS '账户域：1-可用域，2-冻结域，3-待结算域';
COMMENT ON COLUMN zhou6.sys_account_flow.amount IS '流水金额';
COMMENT ON COLUMN zhou6.sys_account_flow.biz_type IS '外部业务类型';
COMMENT ON COLUMN zhou6.sys_account_flow.biz_id IS '外部业务唯一号';
COMMENT ON COLUMN zhou6.sys_account_flow.remark IS '流水备注';
COMMENT ON COLUMN zhou6.sys_account_flow.create_by IS '触发账目变动的操作人';
COMMENT ON COLUMN zhou6.sys_account_flow.create_time IS '实际记账落盘时间';

CREATE INDEX IF NOT EXISTS idx_flow_user_time ON zhou6.sys_account_flow(user_id, create_time DESC);
