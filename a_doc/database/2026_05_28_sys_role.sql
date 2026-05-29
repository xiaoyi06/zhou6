-- ===================================================================
-- 2026_05_29_sys_user_org.sql. 系统角色表
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_role CASCADE;

CREATE TABLE zhou6.sys_role (
                                id int8 NOT NULL, -- 主键ID（雪花算法生成）
                                role_name varchar(50) NOT NULL, -- 角色名称（如：系统管理员、财务审核员）
                                role_code varchar(64) NOT NULL, -- 角色权限编码（如：ADMIN, AUDIT_ROLE，用于后端硬编码权限拦截）
                                data_scope int2 DEFAULT 5 NOT NULL, -- 【核心设计】数据权限范围（1:全部数据 2:自定义机构数据 3:本部门数据 4:本部门及以下数据 5:仅本人数据）
                                sort_order int4 DEFAULT 0 NOT NULL, -- 显示顺序
                                status int2 DEFAULT 1 NOT NULL, -- 角色状态（1:正常 0:停用）
                                remark varchar(255) NULL, -- 角色备注
                                create_time timestamp NULL,
                                create_by int8 NULL,
                                update_time timestamp NULL,
                                update_by int8 NULL,
                                CONSTRAINT sys_role_pkey PRIMARY KEY (id),
                                CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);

COMMENT ON TABLE zhou6.sys_role IS '系统角色表';
COMMENT ON COLUMN zhou6.sys_role.id IS '主键ID（雪花算法生成）';
COMMENT ON COLUMN zhou6.sys_role.role_name IS '角色名称';
COMMENT ON COLUMN zhou6.sys_role.role_code IS '角色权限编码';
COMMENT ON COLUMN zhou6.sys_role.data_scope IS '数据范围（微服务数据隔离核心字段）';
COMMENT ON COLUMN zhou6.sys_role.sort_order IS '显示顺序';
COMMENT ON COLUMN zhou6.sys_role.status IS '角色状态（1:正常 0:停用）';
COMMENT ON COLUMN zhou6.sys_role.remark IS '角色备注';

CREATE INDEX idx_sys_role_name ON zhou6.sys_role USING btree (role_name);
CREATE INDEX idx_sys_role_status ON zhou6.sys_role USING btree (status);

