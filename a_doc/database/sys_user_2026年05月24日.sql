-- zhou6 用户表初始化脚本
-- 数据库：postgres
-- 模式：zhou6
-- 主键策略：代码侧使用 MyBatis-Plus 雪花算法 IdType.ASSIGN_ID 生成 BIGINT 主键

-- 1. 创建并切换模式
CREATE SCHEMA IF NOT EXISTS zhou6;
SET search_path TO zhou6;

-- 2. 创建系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    contact_phone VARCHAR(30),
    personal_signature VARCHAR(255),
    gender SMALLINT DEFAULT 0,
    department_id BIGINT,

    status SMALLINT NOT NULL DEFAULT 1,
    work_status VARCHAR(50),
    is_locked SMALLINT NOT NULL DEFAULT 0,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,

    last_login_ip VARCHAR(45),
    last_login_time TIMESTAMP,

    create_time TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP,
    update_by BIGINT,

    CONSTRAINT uk_sys_user_username UNIQUE (username)
    );

-- 3. 兼容已有表升级
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS personal_signature VARCHAR(255);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS work_status VARCHAR(50);

-- 4. 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_user_department_id ON sys_user (department_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user (status);
CREATE INDEX IF NOT EXISTS idx_sys_user_work_status ON sys_user (work_status);

-- 5. 添加表和字段注释
COMMENT ON TABLE sys_user IS '系统用户表';

COMMENT ON COLUMN sys_user.id IS '主键ID，代码侧使用雪花算法生成';
COMMENT ON COLUMN sys_user.username IS '登录账号';
COMMENT ON COLUMN sys_user.password IS '登录密码';
COMMENT ON COLUMN sys_user.nickname IS '用户昵称';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.contact_phone IS '联系电话';
COMMENT ON COLUMN sys_user.personal_signature IS '个性签名';
COMMENT ON COLUMN sys_user.gender IS '性别 (0:未知 1:男 2:女)';
COMMENT ON COLUMN sys_user.department_id IS '所属部门ID';
COMMENT ON COLUMN sys_user.status IS '账号状态 (1:正常 0:已禁用)';
COMMENT ON COLUMN sys_user.work_status IS '工作状态，取值可来自字典表';
COMMENT ON COLUMN sys_user.is_locked IS '锁定状态 (1:已锁定 0:未锁定)';
COMMENT ON COLUMN sys_user.failed_login_attempts IS '连续登录失败次数';
COMMENT ON COLUMN sys_user.locked_until IS '账号锁定到期时间';
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.create_by IS '创建人ID';
COMMENT ON COLUMN sys_user.update_time IS '修改时间';
COMMENT ON COLUMN sys_user.update_by IS '修改人ID';
