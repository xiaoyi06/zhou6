-- zhou6 文件元数据表初始化脚本
-- 数据库：postgres
-- 模式：zhou6
-- 主键策略：sys_file 代码侧使用 MyBatis-Plus 雪花算法 IdType.ASSIGN_ID 生成 BIGINT 主键

CREATE SCHEMA IF NOT EXISTS zhou6;
SET search_path TO zhou6;

CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(128),
    object_key VARCHAR(512) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    bucket_name VARCHAR(128),
    url VARCHAR(1024),

    create_time TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP,
    update_by BIGINT
);

CREATE INDEX IF NOT EXISTS idx_sys_file_platform ON sys_file (platform);
CREATE INDEX IF NOT EXISTS idx_sys_file_object_key ON sys_file (object_key);
CREATE INDEX IF NOT EXISTS idx_sys_file_create_time ON sys_file (create_time);

COMMENT ON TABLE sys_file IS '统一文件元数据表';
COMMENT ON COLUMN sys_file.id IS '主键ID，代码侧使用雪花算法生成';
COMMENT ON COLUMN sys_file.file_name IS '原始文件名';
COMMENT ON COLUMN sys_file.file_size IS '文件大小，单位字节';
COMMENT ON COLUMN sys_file.content_type IS '文件 MIME 类型';
COMMENT ON COLUMN sys_file.object_key IS '对象存储中的唯一键';
COMMENT ON COLUMN sys_file.platform IS '存储平台标识，如 minio、obs、oss、cos';
COMMENT ON COLUMN sys_file.bucket_name IS '对象存储桶名称';
COMMENT ON COLUMN sys_file.url IS '文件访问地址';
COMMENT ON COLUMN sys_file.create_time IS '创建时间';
COMMENT ON COLUMN sys_file.create_by IS '创建人ID';
COMMENT ON COLUMN sys_file.update_time IS '修改时间';
COMMENT ON COLUMN sys_file.update_by IS '修改人ID';
