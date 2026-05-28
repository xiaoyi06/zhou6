-- ===================================================================
-- 1. 组织机构/部门/班组树形表
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
COMMENT ON COLUMN zhou6.sys_organization.is_deleted IS '逻辑删除（0:未删除 1:已删除）';


-- ===================================================================
-- 2. 全局岗位字典表
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_post CASCADE;

CREATE TABLE zhou6.sys_post (
                                id int8 NOT NULL, -- 主键ID（代码侧使用雪花算法生成）
                                post_code varchar(64) NOT NULL, -- 岗位编码（如：AUDITOR, DEV_LEAD）
                                post_name varchar(50) NOT NULL, -- 岗位名称
                                sort_order int4 DEFAULT 0 NOT NULL, -- 显示顺序
                                status int2 DEFAULT 1 NOT NULL, -- 状态（1:正常 0:停用）
                                create_time timestamp NULL,
                                create_by int8 NULL,
                                update_time timestamp NULL,
                                update_by int8 NULL,
                                CONSTRAINT sys_post_pkey PRIMARY KEY (id),
                                CONSTRAINT uk_sys_post_code UNIQUE (post_code)
);

COMMENT ON TABLE zhou6.sys_post IS '全局岗位字典表';
COMMENT ON COLUMN zhou6.sys_post.id IS '主键ID（代码侧使用雪花算法生成）';
COMMENT ON COLUMN zhou6.sys_post.post_code IS '岗位编码';
COMMENT ON COLUMN zhou6.sys_post.post_name IS '岗位名称';

-- ===================================================================
-- 3. 用户-机构多对多关联表
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_user_org CASCADE;

CREATE TABLE zhou6.sys_user_org (
                                    user_id int8 NOT NULL, -- 用户ID
                                    org_id int8 NOT NULL, -- 机构/部门/班组ID
                                    is_primary int2 DEFAULT 0 NOT NULL, -- 是否为主部门（1:是 0:否，需与 sys_user.primary_org_id 联动保持一致）
                                    CONSTRAINT sys_user_org_pkey PRIMARY KEY (user_id, org_id)
);

CREATE INDEX idx_sys_user_org_org_id ON zhou6.sys_user_org USING btree (org_id);

COMMENT ON TABLE zhou6.sys_user_org IS '用户与机构/部门关联表（多对多关系）';
COMMENT ON COLUMN zhou6.sys_user_org.user_id IS '用户ID';
COMMENT ON COLUMN zhou6.sys_user_org.org_id IS '机构/部门/班组ID';
COMMENT ON COLUMN zhou6.sys_user_org.is_primary IS '是否为主部门（1:是 0:否）';


-- ===================================================================
-- 5. 用户-岗位多对多关联表
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_user_post CASCADE;

CREATE TABLE zhou6.sys_user_post (
                                     user_id int8 NOT NULL, -- 用户ID
                                     post_id int8 NOT NULL, -- 岗位ID
                                     org_id int8 NOT NULL, -- 挂载部门ID（支持在A部门是主管，在B部门是专员的细粒度业务场景）
                                     CONSTRAINT sys_user_post_pkey PRIMARY KEY (user_id, post_id, org_id)
);

CREATE INDEX idx_sys_user_post_post_id ON zhou6.sys_user_post USING btree (post_id);
CREATE INDEX idx_sys_user_post_org_id ON zhou6.sys_user_post USING btree (org_id);
CREATE INDEX idx_sys_user_post_user_id ON zhou6.sys_user_post USING btree (user_id);

COMMENT ON TABLE zhou6.sys_user_post IS '用户与岗位关联表（多对多关系）';
COMMENT ON COLUMN zhou6.sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN zhou6.sys_user_post.post_id IS '岗位ID';
COMMENT ON COLUMN zhou6.sys_user_post.org_id IS '挂载部门ID（区分多部门多岗位的职责范围）';

INSERT INTO "zhou6"."sys_user"("id", "username", "password", "nickname", "email", "contact_phone", "personal_signature", "gender", "department_id", "status", "work_status", "is_locked", "failed_login_attempts", "locked_until", "last_login_ip", "last_login_time", "create_time", "create_by", "update_time", "update_by") VALUES (2058446200079781888, 'zhou6_test', '123456', '测试用户', 'zhou6_test@example.com', '13800000000', NULL, 1, 1001, 1, NULL, 0, 0, NULL, NULL, NULL, '2026-05-24 15:13:13.958425', 0, '2026-05-24 15:13:13.958425', 0);
