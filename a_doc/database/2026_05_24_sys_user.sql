-- ===================================================================
-- 3. 系统用户表（优化纯净版）
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_user CASCADE;

CREATE TABLE zhou6.sys_user (
                                id int8 NOT NULL, -- 主键ID，代码侧使用雪花算法生成
                                username varchar(50) NOT NULL, -- 登录账号
                                "password" varchar(100) NOT NULL, -- 登录密码
                                nickname varchar(50) NULL, -- 用户昵称
                                email varchar(100) NULL, -- 邮箱
                                contact_phone varchar(30) NULL, -- 联系电话
                                avatar_file_id int8 NULL, -- 头像文件ID，引用 file-service 的 sys_file.id
                                personal_signature varchar(255) NULL, -- 个性签名
                                gender int2 DEFAULT 0 NULL, -- 性别 (0:未知 1:男 2:女)
                                primary_org_id int8 NULL, -- 【优化点】主所属机构/部门/班组ID（用于高频单表查询优化）
                                status int2 DEFAULT 1 NOT NULL, -- 账号状态 (1:正常 0:已禁用)
                                work_status varchar(50) NULL, -- 工作状态，取值可来自字典表
                                is_locked int2 DEFAULT 0 NOT NULL, -- 锁定状态 (1:已锁定 0:未锁定)
                                failed_login_attempts int4 DEFAULT 0 NOT NULL, -- 连续登录失败次数
                                locked_until timestamp NULL, -- 账号锁定到期时间
                                last_login_ip varchar(45) NULL, -- 最后登录IP
                                last_login_time timestamp NULL, -- 最后登录时间
                                create_time timestamp NULL, -- 创建时间
                                create_by int8 NULL, -- 创建人ID
                                update_time timestamp NULL, -- 修改时间
                                update_by int8 NULL, -- 修改人ID
                                CONSTRAINT sys_user_pkey PRIMARY KEY (id),
                                CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE INDEX idx_sys_user_primary_org_id ON zhou6.sys_user USING btree (primary_org_id);
CREATE INDEX idx_sys_user_status ON zhou6.sys_user USING btree (status);
CREATE INDEX idx_sys_user_work_status ON zhou6.sys_user USING btree (work_status);

COMMENT ON TABLE zhou6.sys_user IS '系统用户表';
COMMENT ON COLUMN zhou6.sys_user.id IS '主键ID，代码侧使用雪花算法生成';
COMMENT ON COLUMN zhou6.sys_user.username IS '登录账号';
COMMENT ON COLUMN zhou6.sys_user."password" IS '登录密码';
COMMENT ON COLUMN zhou6.sys_user.nickname IS '用户昵称';
COMMENT ON COLUMN zhou6.sys_user.email IS '邮箱';
COMMENT ON COLUMN zhou6.sys_user.contact_phone IS '联系电话';
COMMENT ON COLUMN zhou6.sys_user.avatar_file_id IS '头像文件ID，引用 file-service 的 sys_file.id';
COMMENT ON COLUMN zhou6.sys_user.personal_signature IS '个性签名';
COMMENT ON COLUMN zhou6.sys_user.gender IS '性别 (0:未知 1:男 2:女)';
COMMENT ON COLUMN zhou6.sys_user.primary_org_id IS '主所属机构/部门/班组ID（优化冗余字段，非唯一归属）';
COMMENT ON COLUMN zhou6.sys_user.status IS '账号状态 (1:正常 0:已禁用)';
COMMENT ON COLUMN zhou6.sys_user.work_status IS '工作状态，取值可来自字典表';
COMMENT ON COLUMN zhou6.sys_user.is_locked IS '锁定状态 (1:已锁定 0:未锁定)';
COMMENT ON COLUMN zhou6.sys_user.failed_login_attempts IS '连续登录失败次数';
COMMENT ON COLUMN zhou6.sys_user.locked_until IS '账号锁定到期时间';
COMMENT ON COLUMN zhou6.sys_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN zhou6.sys_user.last_login_time IS '最后登录时间';
