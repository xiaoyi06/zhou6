package com.zhou6.cloud.mybatis.datapermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解，标记在需要自动追加数据范围条件的 Mapper 方法上。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * 当前 SQL 结果集中表示用户 ID 的字段名。
     *
     * @return 用户字段名
     */
    String userAlias() default "user_id";

    /**
     * 当前 SQL 结果集中表示机构/部门 ID 的字段名。
     *
     * @return 机构字段名
     */
    String orgAlias() default "org_id";
}
