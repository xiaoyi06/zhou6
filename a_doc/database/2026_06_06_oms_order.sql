CREATE SCHEMA IF NOT EXISTS zhou6;

CREATE TABLE IF NOT EXISTS zhou6.oms_order (
    id BIGINT PRIMARY KEY,
    order_sn VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount NUMERIC(16, 4) NOT NULL DEFAULT 0.0000,
    pay_amount NUMERIC(16, 4) NOT NULL DEFAULT 0.0000,
    order_status SMALLINT NOT NULL DEFAULT 10,
    create_by BIGINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL DEFAULT 0,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_sn UNIQUE (order_sn)
);

COMMENT ON TABLE zhou6.oms_order IS '订单主表';
COMMENT ON COLUMN zhou6.oms_order.id IS '雪花算法主键ID';
COMMENT ON COLUMN zhou6.oms_order.order_sn IS '唯一订单编号，作为分布式协作的统一biz_id';
COMMENT ON COLUMN zhou6.oms_order.user_id IS '下单用户的用户ID';
COMMENT ON COLUMN zhou6.oms_order.total_amount IS '订单原始总金额';
COMMENT ON COLUMN zhou6.oms_order.pay_amount IS '用户实际应支付的现金金额';
COMMENT ON COLUMN zhou6.oms_order.order_status IS '状态机：10-待支付，20-已支付，30-已取消，40-退款中，50-已退款';
COMMENT ON COLUMN zhou6.oms_order.create_by IS '下单人用户ID';
COMMENT ON COLUMN zhou6.oms_order.create_time IS '订单创建时间';
COMMENT ON COLUMN zhou6.oms_order.update_by IS '最后修改订单状态的操作人ID';
COMMENT ON COLUMN zhou6.oms_order.update_time IS '订单状态最后修改时间';

CREATE INDEX IF NOT EXISTS idx_order_user_status ON zhou6.oms_order(user_id, order_status);

CREATE TABLE IF NOT EXISTS zhou6.oms_order_pay_receipt (
    id BIGINT PRIMARY KEY,
    order_sn VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    freeze_amount NUMERIC(16, 4) NOT NULL,
    receipt_status SMALLINT NOT NULL DEFAULT 1,
    create_by BIGINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL DEFAULT 0,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_receipt_order UNIQUE (order_sn)
);

COMMENT ON TABLE zhou6.oms_order_pay_receipt IS '订单现金扣减凭证表';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.id IS '雪花算法主键ID';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.order_sn IS '订单编号';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.user_id IS '下单用户的用户ID';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.freeze_amount IS '在账户系统中成功冻结的现金数额';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.receipt_status IS '凭证状态：1-已预冻结，2-已确认扣除，3-已反向释放';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.create_by IS '凭证创建人用户ID';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.create_time IS '凭证创建时间';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.update_by IS '最后修改凭证状态的操作人ID';
COMMENT ON COLUMN zhou6.oms_order_pay_receipt.update_time IS '凭证状态最后修改时间';

CREATE INDEX IF NOT EXISTS idx_receipt_user_status ON zhou6.oms_order_pay_receipt(user_id, receipt_status);
