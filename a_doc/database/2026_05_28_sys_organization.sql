-- ===================================================================
-- 2026_05_29_sys_user_org.sql. 组织机构/部门/班组树形表
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_organization CASCADE;

CREATE TABLE zhou6.sys_organization (
                                        id int8 NOT NULL, -- 主键ID（代码侧使用雪花算法生成）
                                        parent_id int8 DEFAULT 0 NOT NULL, -- 父级机构ID（顶级机构为0）
                                        tree_path varchar(255) NULL, -- 祖级路径列表（例如：0,100,101，用于层级穿透查询）
                                        org_name varchar(50) NOT NULL, -- 机构名称（如：某某总公司、研发部、测试一组）
                                        org_type int2 NOT NULL, -- 机构类型（1:单位/公司 2:部门 3:班组）
                                        org_code varchar(64) NULL, -- 机构编码（用于业务核算或唯一标识）
                                        leader_id int8 NULL, -- 负责人用户ID（用于审批流或任务路由）
                                        sort_order int4 DEFAULT 0 NOT NULL, -- 显示顺序
                                        status int2 DEFAULT 1 NOT NULL, -- 状态（1:正常 0:停用）
                                        tree_level int4 DEFAULT 1 NOT NULL, -- 树层级，根部门为1
                                        is_deleted int2 DEFAULT 0 NOT NULL, -- 逻辑删除（0:未删除 1:已删除）
                                        create_time timestamp NULL, -- 创建时间
                                        create_by int8 NULL, -- 创建人ID
                                        update_time timestamp NULL, -- 修改时间
                                        update_by int8 NULL, -- 修改人ID
                                        CONSTRAINT sys_organization_pkey PRIMARY KEY (id),
                                        CONSTRAINT uk_sys_organization_code UNIQUE (org_code)
);

CREATE INDEX idx_sys_org_parent_id ON zhou6.sys_organization USING btree (parent_id);
CREATE INDEX idx_sys_org_tree_path ON zhou6.sys_organization USING btree (tree_path);
CREATE INDEX idx_sys_org_type ON zhou6.sys_organization USING btree (org_type);

COMMENT ON TABLE zhou6.sys_organization IS '组织机构/部门/班组树形表';
COMMENT ON COLUMN zhou6.sys_organization.id IS '主键ID（代码侧使用雪花算法生成）';
COMMENT ON COLUMN zhou6.sys_organization.parent_id IS '父级机构ID（顶级机构为0）';
COMMENT ON COLUMN zhou6.sys_organization.tree_path IS '祖级路径列表（空间换时间，方便层级穿透）';
COMMENT ON COLUMN zhou6.sys_organization.org_name IS '机构名称';
COMMENT ON COLUMN zhou6.sys_organization.org_type IS '机构类型（1:单位/公司 2:部门 3:班组）';
COMMENT ON COLUMN zhou6.sys_organization.org_code IS '机构编码';
COMMENT ON COLUMN zhou6.sys_organization.leader_id IS '负责人用户ID';
COMMENT ON COLUMN zhou6.sys_organization.sort_order IS '显示顺序';
COMMENT ON COLUMN zhou6.sys_organization.status IS '状态（1:正常 0:停用）';
COMMENT ON COLUMN zhou6.sys_organization.tree_level IS '树层级，根部门为1';
COMMENT ON COLUMN zhou6.sys_organization.is_deleted IS '逻辑删除（0:未删除 1l:已删除）';

