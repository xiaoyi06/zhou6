    -- ===================================================================
    -- 2. 用户-角色 关联表 (sys_user_role)
    -- ===================================================================
    DROP TABLE IF EXISTS zhou6.sys_user_role CASCADE;

    CREATE TABLE zhou6.sys_user_role (
                                         user_id int8 NOT NULL, -- 用户ID
                                         role_id int8 NOT NULL, -- 角色ID
                                         CONSTRAINT sys_user_role_pkey PRIMARY KEY (user_id, role_id)
    );

    CREATE INDEX idx_sys_user_role_role_id ON zhou6.sys_user_role USING btree (role_id);
    CREATE INDEX idx_sys_user_role_user_id ON zhou6.sys_user_role USING btree (user_id);

    COMMENT ON TABLE zhou6.sys_user_role IS '用户与角色关联表（多对多关系）';
    COMMENT ON COLUMN zhou6.sys_user_role.user_id IS '用户ID';
    COMMENT ON COLUMN zhou6.sys_user_role.role_id IS '角色ID';


    -- ===================================================================
    -- 3. 角色-机构 数据权限关联表 (sys_role_org) - 可选，用于自定义数据权限
    -- ===================================================================
    -- 只有当 sys_role.data_scope = 2 (自定义机构数据) 时，才需要查这张表
    DROP TABLE IF EXISTS zhou6.sys_role_org CASCADE;

    CREATE TABLE zhou6.sys_role_org (
                                        role_id int8 NOT NULL, -- 角色ID
                                        org_id int8 NOT NULL, -- 机构/部门ID
                                        CONSTRAINT sys_role_org_pkey PRIMARY KEY (role_id, org_id)
    );

    CREATE INDEX idx_sys_role_org_org_id ON zhou6.sys_role_org USING btree (org_id);

    COMMENT ON TABLE zhou6.sys_role_org IS '角色与机构关联表（用于控制自定义数据权限）';
    COMMENT ON COLUMN zhou6.sys_role_org.role_id IS '角色ID';
    COMMENT ON COLUMN zhou6.sys_role_org.org_id IS '机构/部门ID';