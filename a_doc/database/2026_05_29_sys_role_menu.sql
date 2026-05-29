-- ===================================================================
-- 2. 角色-菜单 关联表 (sys_role_menu)
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_role_menu CASCADE;

CREATE TABLE zhou6.sys_role_menu (
                                     role_id int8 NOT NULL,
                                     menu_id int8 NOT NULL,
                                     CONSTRAINT sys_role_menu_pkey PRIMARY KEY (role_id, menu_id)
);

COMMENT ON TABLE zhou6.sys_role_menu IS '角色与菜单关联表';
COMMENT ON COLUMN zhou6.sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN zhou6.sys_role_menu.menu_id IS '菜单ID';

CREATE INDEX idx_sys_role_menu_role_id ON zhou6.sys_role_menu USING btree (role_id);
CREATE INDEX idx_sys_role_menu_menu_id ON zhou6.sys_role_menu USING btree (menu_id);