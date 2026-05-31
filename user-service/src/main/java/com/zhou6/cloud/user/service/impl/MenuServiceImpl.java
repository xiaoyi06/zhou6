package com.zhou6.cloud.user.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.dto.MenuAssignDTO;
import com.zhou6.cloud.user.dto.MenuIdDTO;
import com.zhou6.cloud.user.dto.MenuQueryDTO;
import com.zhou6.cloud.user.dto.MenuSaveDTO;
import com.zhou6.cloud.user.vo.MenuVO;
import com.zhou6.cloud.user.vo.RouterVO;
import com.zhou6.cloud.user.entity.SysMenu;
import com.zhou6.cloud.user.entity.SysRoleMenu;
import com.zhou6.cloud.user.entity.SysUserRole;
import com.zhou6.cloud.user.mapper.SysMenuMapper;
import com.zhou6.cloud.user.mapper.SysRoleMapper;
import com.zhou6.cloud.user.mapper.SysRoleMenuMapper;
import com.zhou6.cloud.user.mapper.SysUserRoleMapper;
import com.zhou6.cloud.user.service.MenuService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 菜单权限业务实现，负责菜单树组装、路由生成和角色菜单关系维护。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final String TYPE_DIR = "M";
    private static final String TYPE_MENU = "C";
    private static final String TYPE_BUTTON = "F";

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final StringRedisTemplate redisTemplate;

    public MenuServiceImpl(SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper,
            SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, StringRedisTemplate redisTemplate) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询菜单树，单次查询后在内存中组装树。
     *
     * @param dto 查询条件
     * @return 菜单树
     */
    @Override
    public List<MenuVO> getTree(MenuQueryDTO dto) {
        LambdaQueryWrapper<SysMenu> wrapper = orderedMenuWrapper();
        if (dto != null && dto.getStatus() != null) {
            wrapper.eq(SysMenu::getStatus, dto.getStatus().shortValue());
        }
        return buildMenuTree(menuMapper.selectList(wrapper).stream().map(this::toMenuVo).toList());
    }

    /**
     * 新增菜单，校验权限标识唯一。
     *
     * @param dto 菜单保存参数
     */
    @Override
    public void add(MenuSaveDTO dto) {
        require(dto != null, "菜单参数不能为空");
        require(hasText(dto.getMenuName()), "菜单名称不能为空");
        require(hasText(dto.getMenuType()), "菜单类型不能为空");
        checkPermsUnique(dto.getPerms(), null);
        Long parentId = parseNullableId(dto.getParentId(), "父菜单ID不正确");
        checkParentExists(parentId);
        SysMenu menu = new SysMenu();
        fillMenu(menu, dto, parentId == null ? ROOT_PARENT_ID : parentId);
        menuMapper.insert(menu);
        clearAllPermissionCache();
    }

    /**
     * 修改菜单，防止父级调整成自身或自身子节点。
     *
     * @param dto 菜单保存参数
     */
    @Override
    public void edit(MenuSaveDTO dto) {
        require(dto != null && hasText(dto.getId()), "菜单ID不能为空");
        require(hasText(dto.getMenuName()), "菜单名称不能为空");
        Long menuId = parseRequiredId(dto.getId(), "菜单ID不正确");
        SysMenu menu = getRequiredMenu(menuId);
        checkPermsUnique(dto.getPerms(), menuId);
        Long parentId = parseNullableId(dto.getParentId(), "父菜单ID不正确");
        if (parentId == null) {
            parentId = ROOT_PARENT_ID;
        }
        require(!Objects.equals(parentId, menuId), "上级菜单不能是自己");
        require(!isChild(menuId, parentId), "上级菜单不能选择自己的子菜单");
        checkParentExists(parentId);
        fillMenu(menu, dto, parentId);
        menuMapper.updateById(menu);
        clearAllPermissionCache();
    }

    /**
     * 删除菜单，存在子菜单或角色关联时禁止删除。
     *
     * @param dto 菜单 ID 参数
     */
    @Override
    public void delete(MenuIdDTO dto) {
        Long menuId = parseRequiredId(dto == null ? null : dto.getId(), "菜单ID不能为空");
        long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        require(childCount == 0, "存在子菜单，不允许删除");
        long roleCount = roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId));
        require(roleCount == 0, "菜单已分配角色，不允许删除");
        menuMapper.deleteById(menuId);
        clearAllPermissionCache();
    }

    /**
     * 查询当前登录用户可见路由，只返回目录和页面菜单。
     *
     * @return 路由树
     */
    @Override
    public List<RouterVO> getRouters() {
        Long userId = UserContextHolder.getUserId();
        require(userId != null, "当前登录已失效，请重新登录");
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<SysMenu> menus = menuMapper.selectList(orderedMenuWrapper()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, StatusConstants.STATUS_ENABLED)
                .in(SysMenu::getMenuType, List.of(TYPE_DIR, TYPE_MENU)));
        return buildRouterTree(menus.stream().map(this::toRouterVo).toList());
    }

    /**
     * 给角色分配菜单，先清理旧关系再批量写入新关系。
     *
     * @param dto 分配参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(MenuAssignDTO dto) {
        require(dto != null, "菜单分配参数不能为空");
        Long roleId = parseRequiredId(dto.getRoleId(), "角色ID不能为空");
        require(roleMapper.selectById(roleId) != null, "角色不存在");
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (dto.getMenuIds() != null) {
            for (String menuIdValue : dto.getMenuIds()) {
                Long menuId = parseRequiredId(menuIdValue, "菜单ID不正确");
                require(menuMapper.selectById(menuId) != null, "菜单不存在");
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
        clearRoleUsersPermissionCache(roleId);
    }

    private void fillMenu(SysMenu menu, MenuSaveDTO dto, Long parentId) {
        menu.setMenuName(dto.getMenuName());
        menu.setParentId(parentId);
        menu.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        menu.setRoutePath(dto.getRoutePath());
        menu.setComponentPath(dto.getComponentPath());
        menu.setMenuType(dto.getMenuType());
        menu.setPerms(dto.getPerms());
        menu.setIcon(hasText(dto.getIcon()) ? dto.getIcon() : "#");
        menu.setVisible(dto.getVisible() == null ? StatusConstants.VISIBLE_YES : dto.getVisible().shortValue());
        menu.setStatus(dto.getStatus() == null ? StatusConstants.STATUS_ENABLED : dto.getStatus().shortValue());
    }

    private boolean isChild(Long selfId, Long maybeChildId) {
        if (Objects.equals(maybeChildId, ROOT_PARENT_ID)) {
            return false;
        }
        Long currentId = maybeChildId;
        while (!Objects.equals(currentId, ROOT_PARENT_ID)) {
            SysMenu current = menuMapper.selectById(currentId);
            if (current == null) {
                return false;
            }
            if (Objects.equals(current.getParentId(), selfId)) {
                return true;
            }
            currentId = current.getParentId();
        }
        return false;
    }

    private void checkParentExists(Long parentId) {
        if (parentId == null || Objects.equals(parentId, ROOT_PARENT_ID)) {
            return;
        }
        require(menuMapper.selectById(parentId) != null, "上级菜单不存在");
    }

    private void checkPermsUnique(String perms, Long selfId) {
        if (!hasText(perms)) {
            return;
        }
        SysMenu menu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getPerms, perms)
                .last("limit 1"));
        require(menu == null || Objects.equals(menu.getId(), selfId), "权限标识已存在");
    }

    private List<MenuVO> buildMenuTree(List<MenuVO> nodes) {
        Map<String, MenuVO> nodeMap = new LinkedHashMap<>();
        for (MenuVO node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : nodes) {
            MenuVO parent = nodeMap.get(node.getParentId());
            if (parent == null || Objects.equals(node.getParentId(), String.valueOf(ROOT_PARENT_ID))) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        sortMenuTree(roots);
        return roots;
    }

    private List<RouterVO> buildRouterTree(List<RouterVO> nodes) {
        Map<String, RouterVO> nodeMap = new LinkedHashMap<>();
        for (RouterVO node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        List<RouterVO> roots = new ArrayList<>();
        for (RouterVO node : nodes) {
            RouterVO parent = nodeMap.get(node.getParentId());
            if (parent == null || Objects.equals(node.getParentId(), String.valueOf(ROOT_PARENT_ID))) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
                parent.getPerms().addAll(node.getPerms());
            }
        }
        return roots;
    }

    private void sortMenuTree(List<MenuVO> nodes) {
        nodes.sort(Comparator.comparing(MenuVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuVO::getId));
        for (MenuVO node : nodes) {
            sortMenuTree(node.getChildren());
        }
    }

    private LambdaQueryWrapper<SysMenu> orderedMenuWrapper() {
        return new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId);
    }

    private MenuVO toMenuVo(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(String.valueOf(menu.getId()));
        vo.setMenuName(menu.getMenuName());
        vo.setParentId(String.valueOf(menu.getParentId()));
        vo.setSortOrder(menu.getSortOrder());
        vo.setRoutePath(menu.getRoutePath());
        vo.setComponentPath(menu.getComponentPath());
        vo.setMenuType(menu.getMenuType());
        vo.setPerms(menu.getPerms());
        vo.setIcon(menu.getIcon());
        vo.setVisible(menu.getVisible() == null ? null : menu.getVisible().intValue());
        vo.setStatus(menu.getStatus() == null ? null : menu.getStatus().intValue());
        return vo;
    }

    private RouterVO toRouterVo(SysMenu menu) {
        RouterVO vo = new RouterVO();
        vo.setId(String.valueOf(menu.getId()));
        vo.setParentId(String.valueOf(menu.getParentId()));
        vo.setName(menu.getMenuName());
        vo.setPath(menu.getRoutePath());
        vo.setComponent(menu.getComponentPath());
        vo.setIcon(menu.getIcon());
        vo.setHidden(!Objects.equals(menu.getVisible(), StatusConstants.VISIBLE_YES));
        if (hasText(menu.getPerms())) {
            vo.getPerms().add(menu.getPerms());
        }
        vo.setSortOrder(menu.getSortOrder());
        return vo;
    }

    private SysMenu getRequiredMenu(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        require(menu != null, "菜单不存在");
        return menu;
    }

    private void clearRoleUsersPermissionCache(Long roleId) {
        List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
        for (Long userId : userIds) {
            clearPermissionCache(userId);
        }
    }

    private void clearAllPermissionCache() {
        redisTemplate.delete("zhou6:user:permissions:all");
    }

    private void clearPermissionCache(Long userId) {
        String userIdValue = String.valueOf(userId);
        redisTemplate.delete("zhou6:user:permissions:" + userIdValue);
        redisTemplate.delete("zhou6:user:permission:" + userIdValue);
        redisTemplate.delete("zhou6:user:routers:" + userIdValue);
    }

    private Long parseRequiredId(String value, String message) {
        require(hasText(value), message);
        return parseNullableId(value, message);
    }

    private Long parseNullableId(String value, String message) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
