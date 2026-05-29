-- zhou6 岗位表和用户岗位关联表初始化脚本
-- 数据库：postgres
-- 模式：zhou6
-- 主键策略：sys_post 代码侧使用 MyBatis-Plus 雪花算法 IdType.ASSIGN_ID 生成 BIGINT 主键

-- 2026_05_29_sys_user_org.sql. 创建并切换模式
CREATE SCHEMA IF NOT EXISTS zhou6;
SET search_path TO zhou6;

-- 2. 创建岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT PRIMARY KEY,
    post_code VARCHAR(64) NOT NULL,
    post_name VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,

    create_time TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP,
    update_by BIGINT,

    CONSTRAINT uk_sys_post_code UNIQUE (post_code)
    );

-- 4. 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_post_status ON sys_post (status);
CREATE INDEX IF NOT EXISTS idx_sys_post_code ON sys_post (post_code);


-- 5. 添加表和字段注释
COMMENT ON TABLE sys_post IS '全局岗位字典表';
COMMENT ON COLUMN sys_post.id IS '主键ID，代码侧使用雪花算法生成';
COMMENT ON COLUMN sys_post.post_code IS '岗位编码，全局唯一';
COMMENT ON COLUMN sys_post.post_name IS '岗位名称';
COMMENT ON COLUMN sys_post.sort_order IS '显示顺序';
COMMENT ON COLUMN sys_post.status IS '状态 (1:正常 0:停用)';
COMMENT ON COLUMN sys_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_post.create_by IS '创建人ID';
COMMENT ON COLUMN sys_post.update_time IS '修改时间';
COMMENT ON COLUMN sys_post.update_by IS '修改人ID';
