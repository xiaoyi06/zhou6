-- zhou6 用户头像字段迁移脚本
-- 数据库：postgres
-- 模式：zhou6
-- 说明：将 sys_user.avatar 改为 sys_user.avatar_file_id，只保存 file-service 的 sys_file.id。

SET search_path TO zhou6;

-- 1. 字段改名：avatar -> avatar_file_id。
ALTER TABLE sys_user RENAME COLUMN avatar TO avatar_file_id;

-- 2. 清理旧占位值或非数字值，避免 varchar 转 BIGINT 失败。
UPDATE zhou6.sys_user
SET avatar_file_id = NULL
WHERE avatar_file_id IS NOT NULL
  AND avatar_file_id::TEXT !~ '^[0-9]+$';

-- 3. 字段类型改为 BIGINT，与 sys_file.id 保持一致。
ALTER TABLE zhou6.sys_user
    ALTER COLUMN avatar_file_id TYPE BIGINT USING NULLIF(avatar_file_id::TEXT, '')::BIGINT;

-- 4. 更新字段注释和查询索引。
COMMENT ON COLUMN zhou6.sys_user.avatar_file_id IS '头像文件ID，引用 file-service 的 sys_file.id';

CREATE INDEX IF NOT EXISTS idx_sys_user_avatar_file_id ON zhou6.sys_user (avatar_file_id);
