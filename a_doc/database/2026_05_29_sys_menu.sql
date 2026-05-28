-- ===================================================================
-- 1. 系统菜单权限表 (sys_menu)
-- ===================================================================
DROP TABLE IF EXISTS zhou6.sys_menu CASCADE;

CREATE TABLE zhou6.sys_menu (
                                id int8 NOT NULL,               -- 主键ID
                                menu_name varchar(50) NOT NULL, -- 菜单名称
                                parent_id int8 DEFAULT 0 NOT NULL, -- 父菜单ID
                                sort_order int4 DEFAULT 0 NOT NULL, -- 显示顺序
                                route_path varchar(200),        -- 路由地址（如: /system/user）
                                component_path varchar(255),    -- 组件路径（如: system/user/index）
                                menu_type char(1) NOT NULL,     -- 类型: M目录, C菜单, F按钮
                                perms varchar(100),             -- 权限标识（如: sys:user:add）
                                icon varchar(100) DEFAULT '#',  -- 菜单图标
                                visible int2 DEFAULT 1,         -- 显示状态: 1显示, 0隐藏
                                status int2 DEFAULT 1,          -- 状态: 1正常, 0停用
                                create_time timestamp,
                                create_by int8,
                                update_time timestamp,
                                update_by int8,
                                CONSTRAINT sys_menu_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE zhou6.sys_menu IS '系统菜单权限表';
COMMENT ON COLUMN zhou6.sys_menu.id IS '主键ID';
COMMENT ON COLUMN zhou6.sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN zhou6.sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN zhou6.sys_menu.sort_order IS '显示顺序';
COMMENT ON COLUMN zhou6.sys_menu.route_path IS '路由地址';
COMMENT ON COLUMN zhou6.sys_menu.component_path IS '组件路径';
COMMENT ON COLUMN zhou6.sys_menu.menu_type IS 'M:目录, C:页面, F:按钮';
COMMENT ON COLUMN zhou6.sys_menu.perms IS '权限标识';
COMMENT ON COLUMN zhou6.sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN zhou6.sys_menu.visible IS '显示状态: 1显示, 0隐藏';
COMMENT ON COLUMN zhou6.sys_menu.status IS '状态: 1正常, 0停用';

CREATE INDEX idx_sys_menu_parent_id ON zhou6.sys_menu USING btree (parent_id);
CREATE INDEX idx_sys_menu_status ON zhou6.sys_menu USING btree (status);
CREATE INDEX idx_sys_menu_perms ON zhou6.sys_menu USING btree (perms);

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
