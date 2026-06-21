-- 外部系统表及角色所属系统字段迁移脚本（PostgreSQL / zhou6 schema）
-- 角色新增、修改接口要求 system_id 必填；为兼容已有角色数据，本脚本保留 system_id 可空，
-- 完成历史角色回填后可由运维执行最后的 NOT NULL 约束。

CREATE SCHEMA IF NOT EXISTS zhou6;
SET search_path TO zhou6;

CREATE TABLE IF NOT EXISTS sys_external_system (
    id BIGINT PRIMARY KEY,
    system_name VARCHAR(50) NOT NULL,
    system_code VARCHAR(64) NOT NULL,
    system_url VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_time TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP,
    update_by BIGINT,
    CONSTRAINT uk_sys_external_system_code UNIQUE (system_code)
);

COMMENT ON TABLE sys_external_system IS '一站式服务接入的外部系统表';
COMMENT ON COLUMN sys_external_system.id IS '主键ID，代码侧使用雪花算法生成';
COMMENT ON COLUMN sys_external_system.system_name IS '外部系统名称';
COMMENT ON COLUMN sys_external_system.system_code IS '外部系统编码，全局唯一';
COMMENT ON COLUMN sys_external_system.system_url IS '外部系统访问地址';
COMMENT ON COLUMN sys_external_system.sort_order IS '显示顺序';
COMMENT ON COLUMN sys_external_system.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN sys_external_system.remark IS '备注';

CREATE INDEX IF NOT EXISTS idx_sys_external_system_status ON sys_external_system (status);
CREATE INDEX IF NOT EXISTS idx_sys_external_system_code ON sys_external_system (system_code);

ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS system_id BIGINT;
COMMENT ON COLUMN sys_role.system_id IS '所属外部系统ID，关联 sys_external_system.id';
CREATE INDEX IF NOT EXISTS idx_sys_role_system_id ON sys_role (system_id);

-- 历史角色完成 system_id 回填后执行：
-- ALTER TABLE sys_role ALTER COLUMN system_id SET NOT NULL;

