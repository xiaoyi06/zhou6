DROP TABLE IF EXISTS zhou6.sys_user_post CASCADE;
-- 3. 创建用户岗位关联表
CREATE TABLE IF NOT EXISTS sys_user_post (
                                             user_id BIGINT NOT NULL,
                                             post_id BIGINT NOT NULL,
                                             org_id BIGINT NOT NULL,

                                             CONSTRAINT sys_user_post_pkey PRIMARY KEY (user_id, post_id, org_id)
    );
COMMENT ON TABLE sys_user_post IS '用户与岗位关联表，支持用户在不同部门拥有不同岗位';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID';
COMMENT ON COLUMN sys_user_post.org_id IS '挂载部门ID，限定岗位生效范围';
CREATE INDEX IF NOT EXISTS idx_sys_user_post_user_id ON sys_user_post (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_post_id ON sys_user_post (post_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_org_id ON sys_user_post (org_id);

