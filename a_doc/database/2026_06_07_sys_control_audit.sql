CREATE SCHEMA IF NOT EXISTS zhou6;
SET search_path TO zhou6;

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_type VARCHAR(100) NOT NULL,
    is_status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
);

COMMENT ON TABLE sys_dict_type IS '系统字典类型表';
COMMENT ON COLUMN sys_dict_type.id IS '字典主键，雪花ID';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称，如白名单类型';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型，如sys_whitelist_type';
COMMENT ON COLUMN sys_dict_type.is_status IS '状态：0-禁用，1-正常';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';
COMMENT ON COLUMN sys_dict_type.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_type.update_time IS '更新时间';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT PRIMARY KEY,
    dict_type VARCHAR(100) NOT NULL,
    dict_label VARCHAR(100) NOT NULL,
    dict_value VARCHAR(100) NOT NULL,
    dict_sort INT NOT NULL DEFAULT 0,
    is_default SMALLINT NOT NULL DEFAULT 0,
    is_status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dict_data_type ON sys_dict_data(dict_type);
CREATE UNIQUE INDEX IF NOT EXISTS uk_dict_data_type_value ON sys_dict_data(dict_type, dict_value);

COMMENT ON TABLE sys_dict_data IS '系统字典数据表';
COMMENT ON COLUMN sys_dict_data.id IS '字典数据主键，雪花ID';
COMMENT ON COLUMN sys_dict_data.dict_type IS '字典类型，关联sys_dict_type.dict_type';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签，展示文本';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值，业务存储值';
COMMENT ON COLUMN sys_dict_data.dict_sort IS '字典排序，数字越小越靠前';
COMMENT ON COLUMN sys_dict_data.is_default IS '是否默认：0-否，1-是';
COMMENT ON COLUMN sys_dict_data.is_status IS '状态：0-禁用，1-正常';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
COMMENT ON COLUMN sys_dict_data.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_data.update_time IS '更新时间';

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value JSONB NOT NULL,
    config_name VARCHAR(100) NOT NULL,
    is_status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_config_key UNIQUE (config_key)
);

COMMENT ON TABLE sys_config IS '系统控制配置表';
COMMENT ON COLUMN sys_config.id IS '主键ID，雪花ID';
COMMENT ON COLUMN sys_config.config_key IS '配置键名，例如sys.maintenance.mode';
COMMENT ON COLUMN sys_config.config_value IS '配置值，JSONB格式';
COMMENT ON COLUMN sys_config.config_name IS '配置名称';
COMMENT ON COLUMN sys_config.is_status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN sys_config.remark IS '备注';
COMMENT ON COLUMN sys_config.update_time IS '更新时间';

CREATE TABLE IF NOT EXISTS sys_whitelist (
    id BIGINT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    value VARCHAR(255) NOT NULL,
    is_status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_whitelist_type_value ON sys_whitelist(type, value);
CREATE INDEX IF NOT EXISTS idx_whitelist_status ON sys_whitelist(is_status);

COMMENT ON TABLE sys_whitelist IS '系统白名单表';
COMMENT ON COLUMN sys_whitelist.id IS '主键ID，雪花ID';
COMMENT ON COLUMN sys_whitelist.type IS '白名单类型：IP、USER、ROUTE';
COMMENT ON COLUMN sys_whitelist.value IS '具体白名单值，例如IP、用户ID、API路由';
COMMENT ON COLUMN sys_whitelist.is_status IS '是否启用：0-禁用，1-启用';
COMMENT ON COLUMN sys_whitelist.remark IS '备注';
COMMENT ON COLUMN sys_whitelist.create_time IS '创建时间';

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGSERIAL,
    user_id BIGINT,
    username VARCHAR(50),
    ip_address INET,
    login_location VARCHAR(255),
    browser VARCHAR(50),
    os VARCHAR(50),
    status SMALLINT NOT NULL DEFAULT 1,
    msg VARCHAR(255),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, login_time)
) PARTITION BY RANGE (login_time);

COMMENT ON TABLE sys_login_log IS '系统登录记录表，按月声明式分区';
COMMENT ON COLUMN sys_login_log.id IS '主键ID，分区表与分区键组成复合主键';
COMMENT ON COLUMN sys_login_log.user_id IS '用户ID';
COMMENT ON COLUMN sys_login_log.username IS '登录账号';
COMMENT ON COLUMN sys_login_log.ip_address IS '登录IP，PostgreSQL INET类型';
COMMENT ON COLUMN sys_login_log.login_location IS '登录地点';
COMMENT ON COLUMN sys_login_log.browser IS '浏览器类型';
COMMENT ON COLUMN sys_login_log.os IS '操作系统';
COMMENT ON COLUMN sys_login_log.status IS '登录状态：1-成功，0-失败，-1-账号冻结';
COMMENT ON COLUMN sys_login_log.msg IS '提示消息';
COMMENT ON COLUMN sys_login_log.login_time IS '登录时间';

CREATE TABLE IF NOT EXISTS sys_login_log_2026_06 PARTITION OF sys_login_log
    FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');
CREATE TABLE IF NOT EXISTS sys_login_log_2026_07 PARTITION OF sys_login_log
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');
CREATE INDEX IF NOT EXISTS idx_login_log_user_time ON sys_login_log(user_id, login_time DESC);

CREATE TABLE IF NOT EXISTS sys_traffic_stat (
    id BIGSERIAL PRIMARY KEY,
    api_route VARCHAR(255) NOT NULL,
    pv BIGINT NOT NULL DEFAULT 0,
    uv BIGINT NOT NULL DEFAULT 0,
    avg_rt INT NOT NULL DEFAULT 0,
    stat_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_route_time UNIQUE (api_route, stat_time)
);

COMMENT ON TABLE sys_traffic_stat IS '流量监控统计表';
COMMENT ON COLUMN sys_traffic_stat.id IS '主键ID';
COMMENT ON COLUMN sys_traffic_stat.api_route IS '请求路由或接口路径';
COMMENT ON COLUMN sys_traffic_stat.pv IS '访问量';
COMMENT ON COLUMN sys_traffic_stat.uv IS '独立访客数';
COMMENT ON COLUMN sys_traffic_stat.avg_rt IS '平均响应时间，毫秒';
COMMENT ON COLUMN sys_traffic_stat.stat_time IS '统计时间段，默认精确到5分钟';

CREATE TABLE IF NOT EXISTS sys_menu_usage_stat (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    menu_name VARCHAR(100),
    route_path VARCHAR(255),
    use_count BIGINT NOT NULL DEFAULT 0,
    last_use_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_menu_usage_user_menu UNIQUE (user_id, menu_id)
);

CREATE INDEX IF NOT EXISTS idx_menu_usage_user_rank ON sys_menu_usage_stat(user_id, use_count DESC, last_use_time DESC);

COMMENT ON TABLE sys_menu_usage_stat IS '用户常用菜单统计表';
COMMENT ON COLUMN sys_menu_usage_stat.id IS '主键ID';
COMMENT ON COLUMN sys_menu_usage_stat.user_id IS '用户ID';
COMMENT ON COLUMN sys_menu_usage_stat.menu_id IS '菜单ID，对应user-service.sys_menu.id';
COMMENT ON COLUMN sys_menu_usage_stat.menu_name IS '菜单名称快照';
COMMENT ON COLUMN sys_menu_usage_stat.route_path IS '菜单路由快照';
COMMENT ON COLUMN sys_menu_usage_stat.use_count IS '使用次数';
COMMENT ON COLUMN sys_menu_usage_stat.last_use_time IS '最后使用时间';

-- 网关追踪 API 调用统计表：由 gateway-service MenuUsageWebFilter 写入 Redis，sys-service 定时刷库。
CREATE TABLE IF NOT EXISTS sys_api_usage_stat (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    module_name VARCHAR(50) NOT NULL,
    api_path VARCHAR(255) NOT NULL,
    use_count BIGINT NOT NULL DEFAULT 0,
    last_access_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_api_usage_user_module UNIQUE (user_id, module_name)
);

CREATE INDEX IF NOT EXISTS idx_api_usage_user_rank ON sys_api_usage_stat(user_id, use_count DESC, last_access_time DESC);

COMMENT ON TABLE sys_api_usage_stat IS '应用/菜单调用频率统计表（网关自动追踪）';
COMMENT ON COLUMN sys_api_usage_stat.id IS '主键ID';
COMMENT ON COLUMN sys_api_usage_stat.user_id IS '用户ID';
COMMENT ON COLUMN sys_api_usage_stat.module_name IS '应用/菜单标识，如 user-api/role（角色管理）、user-api/user（用户管理）、sys-api/dict（字典管理）';
COMMENT ON COLUMN sys_api_usage_stat.api_path IS '请求路径前缀，如 /api/v1/user-api/role';
COMMENT ON COLUMN sys_api_usage_stat.use_count IS '调用次数';
COMMENT ON COLUMN sys_api_usage_stat.last_access_time IS '最后调用时间';

INSERT INTO sys_dict_type (id, dict_name, dict_type, remark)
VALUES
    (700000000000000001, '白名单类型', 'sys_whitelist_type', '安全白名单拦截维度'),
    (700000000000000002, '登录状态', 'sys_login_status', '审计登录结果'),
    (700000000000000003, 'API模块名称', 'sys_api_module_name', 'API 调用统计模块的中文显示名称')
ON CONFLICT (dict_type) DO NOTHING;

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort)
VALUES
    (700000000000000101, 'sys_whitelist_type', 'IP白名单', 'IP', 1),
    (700000000000000102, 'sys_whitelist_type', '用户白名单', 'USER', 2),
    (700000000000000103, 'sys_whitelist_type', '路由白名单', 'ROUTE', 3),
    (700000000000000201, 'sys_login_status', '成功', '1', 1),
    (700000000000000202, 'sys_login_status', '密码错误', '0', 2),
    (700000000000000203, 'sys_login_status', '账号冻结', '-1', 3),
    (700000000000000301, 'sys_api_module_name', '用户管理', 'user-api/userManagement', 1),
    (700000000000000302, 'sys_api_module_name', '角色管理', 'user-api/role', 2),
    (700000000000000303, 'sys_api_module_name', '菜单管理', 'user-api/menu', 3),
    (700000000000000304, 'sys_api_module_name', '组织管理', 'user-api/organization', 4),
    (700000000000000305, 'sys_api_module_name', '岗位管理', 'user-api/post', 5),
    (700000000000000306, 'sys_api_module_name', '个人信息', 'user-api/userInfo', 6),
    (700000000000000307, 'sys_api_module_name', '字典管理', 'sys-api/dict', 7),
    (700000000000000308, 'sys_api_module_name', '配置管理', 'sys-api/config', 8),
    (700000000000000309, 'sys_api_module_name', '白名单管理', 'sys-api/whitelist', 9),
    (700000000000000310, 'sys_api_module_name', '审计日志', 'sys-api/audit', 10),
    (700000000000000311, 'sys_api_module_name', '流量统计', 'sys-api/traffic', 11),
    (700000000000000312, 'sys_api_module_name', '菜单统计', 'sys-api/menuUsage', 12),
    (700000000000000313, 'sys_api_module_name', '订单管理', 'order-api/order', 13),
    (700000000000000314, 'sys_api_module_name', '退款管理', 'order-api/refund', 14),
    (700000000000000315, 'sys_api_module_name', '账户管理', 'account-api/account-api', 15),
    (700000000000000316, 'sys_api_module_name', '流程待办', 'workflow-api/process', 16),
    (700000000000000317, 'sys_api_module_name', '流程管理', 'workflow-api/inner', 17),
    (700000000000000318, 'sys_api_module_name', '文件管理', 'file-api/file-api', 18)
ON CONFLICT (dict_type, dict_value) DO NOTHING;

INSERT INTO sys_config (id, config_key, config_value, config_name, remark)
VALUES
    (700000000000001001, 'sys.maintenance.mode', 'false'::jsonb, '全站系统维护开关', '开启后非白名单用户将无法访问系统'),
    (700000000000001002, 'sys.traffic.monitor', 'true'::jsonb, '全局流量监控开关', '控制是否开启流量统计')
ON CONFLICT (config_key) DO NOTHING;
