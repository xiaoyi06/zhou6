package com.zhou6.cloud.user.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 系统角色实体，对应 sys_role 表。
 */
@TableName("sys_role")
public class SysRole {

    /** 主键ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色名称。 */
    private String roleName;

    /** 角色编码，全局唯一且不允许修改。 */
    private String roleCode;

    /** 所属外部系统ID。 */
    private Long systemId;

    /** 数据权限范围，1全部，2自定义，3本部门，4本部门及以下，5仅本人。 */
    private Short dataScope;

    /** 排序号。 */
    private Integer sortOrder;

    /** 角色状态，1正常，0停用。 */
    private Short status;

    /** 备注。 */
    private String remark;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 修改时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 修改人ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public Short getDataScope() {
        return dataScope;
    }

    public void setDataScope(Short dataScope) {
        this.dataScope = dataScope;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }
}
