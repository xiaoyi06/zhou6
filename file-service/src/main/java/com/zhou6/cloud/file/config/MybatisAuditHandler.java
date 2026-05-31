package com.zhou6.cloud.file.config;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zhou6.cloud.common.context.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * file-service MyBatis-Plus 审计字段自动填充处理器。
 * <p>
 * SysFile 中的 createTime/createBy/updateTime/updateBy 依赖该 Bean 自动写入。
 */
@Component
public class MybatisAuditHandler implements MetaObjectHandler {

    /**
     * 新增文件元数据时填充创建和修改审计字段。
     *
     * @param metaObject MyBatis-Plus 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContextHolder.getUserId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        if (userId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    /**
     * 修改文件元数据时刷新修改审计字段。
     *
     * @param metaObject MyBatis-Plus 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        Long userId = UserContextHolder.getUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }
    }
}
