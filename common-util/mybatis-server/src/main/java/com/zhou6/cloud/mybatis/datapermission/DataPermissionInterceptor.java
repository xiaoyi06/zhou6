package com.zhou6.cloud.mybatis.datapermission;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zhou6.cloud.common.context.UserContextHolder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * MyBatis 数据权限拦截器，根据 Mapper 方法上的 DataPermission 注解自动追加数据范围 SQL。
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class DataPermissionInterceptor implements Interceptor {

    private static final short DATA_SCOPE_ALL = 1;
    private static final short DATA_SCOPE_CUSTOM = 2;
    private static final short DATA_SCOPE_DEPT = 3;
    private static final short DATA_SCOPE_DEPT_AND_CHILD = 4;
    private static final short DATA_SCOPE_SELF = 5;

    /**
     * 拦截查询 SQL，并在必要时追加数据权限条件。
     *
     * @param invocation MyBatis 调用上下文
     * @return 查询结果
     * @throws Throwable 查询异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Executor executor = (Executor) invocation.getTarget();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        DataPermission dataPermission = findDataPermission(mappedStatement);
        Long currentUserId = UserContextHolder.getUserId();
        if (dataPermission == null || currentUserId == null || !isSelect(mappedStatement)) {
            return invocation.proceed();
        }

        BoundSql boundSql = args.length == 6 ? (BoundSql) args[5] : mappedStatement.getBoundSql(args[1]);
        String permissionSql = buildPermissionSql(executor.getTransaction().getConnection(), currentUserId,
                dataPermission, boundSql.getSql());
        if (permissionSql == null) {
            return invocation.proceed();
        }
        setSql(boundSql, permissionSql);
        if (args.length == 6 && args[4] instanceof CacheKey cacheKey) {
            cacheKey.update(permissionSql);
            return invocation.proceed();
        }
        CacheKey cacheKey = executor.createCacheKey(mappedStatement, args[1], (RowBounds) args[2], boundSql);
        cacheKey.update(permissionSql);
        return executor.query(mappedStatement, args[1], (RowBounds) args[2], (ResultHandler<?>) args[3], cacheKey, boundSql);
    }

    /**
     * 包装目标对象。
     *
     * @param target 目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置插件属性。
     *
     * @param properties 插件属性
     */
    @Override
    public void setProperties(Properties properties) {
    }

    private String buildPermissionSql(Connection connection, Long currentUserId, DataPermission dataPermission,
            String originalSql) throws Exception {
        DataScopeContext context = readDataScopeContext(connection, currentUserId);
        if (context.dataScope == null || context.dataScope == DATA_SCOPE_ALL) {
            return null;
        }
        String condition = buildCondition(connection, context, dataPermission);
        if (condition == null || condition.isBlank()) {
            condition = "1 = 0";
        }
        String sql = trimSemicolon(originalSql);
        return "SELECT * FROM (" + sql + ") zhou6_dp WHERE " + condition;
    }

    private DataScopeContext readDataScopeContext(Connection connection, Long currentUserId) throws Exception {
        DataScopeContext context = new DataScopeContext();
        context.userId = currentUserId;
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT u.primary_org_id, r.id, r.data_scope
                FROM zhou6.sys_user u
                LEFT JOIN zhou6.sys_user_role ur ON ur.user_id = u.id
                LEFT JOIN zhou6.sys_role r ON r.id = ur.role_id AND r.status = 1
                WHERE u.id = ?
                """)) {
            statement.setLong(1, currentUserId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    if (context.primaryOrgId == null) {
                        long primaryOrgId = rs.getLong("primary_org_id");
                        context.primaryOrgId = rs.wasNull() ? null : primaryOrgId;
                    }
                    long roleId = rs.getLong("id");
                    if (!rs.wasNull()) {
                        context.roleIds.add(roleId);
                    }
                    short dataScope = rs.getShort("data_scope");
                    if (!rs.wasNull()) {
                        context.dataScope = mostPermissive(context.dataScope, dataScope);
                    }
                }
            }
        }
        if (context.dataScope == null) {
            context.dataScope = DATA_SCOPE_SELF;
        }
        return context;
    }

    private Short mostPermissive(Short current, short candidate) {
        if (current == null) {
            return candidate;
        }
        return candidate < current ? candidate : current;
    }

    private String buildCondition(Connection connection, DataScopeContext context, DataPermission dataPermission)
            throws Exception {
        String userColumn = column(dataPermission.userAlias());
        String orgColumn = column(dataPermission.orgAlias());
        if (context.dataScope == DATA_SCOPE_SELF) {
            return userColumn + " = " + context.userId;
        }
        if (context.primaryOrgId == null && (context.dataScope == DATA_SCOPE_DEPT
                || context.dataScope == DATA_SCOPE_DEPT_AND_CHILD)) {
            return "1 = 0";
        }
        if (context.dataScope == DATA_SCOPE_DEPT) {
            return orgColumn + " = " + context.primaryOrgId;
        }
        if (context.dataScope == DATA_SCOPE_DEPT_AND_CHILD) {
            List<Long> orgIds = readSelfAndChildrenOrgIds(connection, context.primaryOrgId);
            return inCondition(orgColumn, orgIds);
        }
        if (context.dataScope == DATA_SCOPE_CUSTOM) {
            List<Long> orgIds = readCustomOrgIds(connection, context.roleIds);
            return inCondition(orgColumn, orgIds);
        }
        return null;
    }

    private List<Long> readCustomOrgIds(Connection connection, List<Long> roleIds) throws Exception {
        if (roleIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", roleIds.stream().map(id -> "?").toList());
        List<Long> orgIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT org_id FROM zhou6.sys_role_org WHERE role_id IN (" + placeholders + ")")) {
            for (int i = 0; i < roleIds.size(); i++) {
                statement.setLong(i + 1, roleIds.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    orgIds.add(rs.getLong("org_id"));
                }
            }
        }
        return orgIds;
    }

    private List<Long> readSelfAndChildrenOrgIds(Connection connection, Long primaryOrgId) throws Exception {
        List<Long> orgIds = new ArrayList<>();
        String treePath = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT tree_path FROM zhou6.sys_organization WHERE id = ?")) {
            statement.setLong(1, primaryOrgId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    treePath = rs.getString("tree_path");
                }
            }
        }
        if (treePath == null || treePath.isBlank()) {
            orgIds.add(primaryOrgId);
            return orgIds;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM zhou6.sys_organization WHERE tree_path LIKE ?")) {
            statement.setString(1, treePath + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    orgIds.add(rs.getLong("id"));
                }
            }
        }
        return orgIds;
    }

    private String inCondition(String column, List<Long> values) {
        if (values == null || values.isEmpty()) {
            return "1 = 0";
        }
        return column + " IN (" + String.join(",", values.stream().map(String::valueOf).toList()) + ")";
    }

    private DataPermission findDataPermission(MappedStatement mappedStatement) {
        String statementId = mappedStatement.getId();
        int lastDotIndex = statementId.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return null;
        }
        String className = statementId.substring(0, lastDotIndex);
        String methodName = statementId.substring(lastDotIndex + 1);
        try {
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(DataPermission.class)) {
                    return method.getAnnotation(DataPermission.class);
                }
            }
        } catch (ClassNotFoundException ignored) {
            return null;
        }
        return null;
    }

    private boolean isSelect(MappedStatement mappedStatement) {
        return mappedStatement.getSqlCommandType() == org.apache.ibatis.mapping.SqlCommandType.SELECT;
    }

    private void setSql(BoundSql boundSql, String sql) throws Exception {
        Field field = BoundSql.class.getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, sql);
    }

    private String trimSemicolon(String sql) {
        String value = sql == null ? "" : sql.trim();
        while (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1).trim();
        }
        return value;
    }

    private String column(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_\\.]+")) {
            throw new IllegalArgumentException("数据权限字段名不合法");
        }
        return value;
    }

    private static final class DataScopeContext {
        private Long userId;
        private Long primaryOrgId;
        private Short dataScope;
        private final List<Long> roleIds = new ArrayList<>();
    }
}
