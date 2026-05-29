
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
