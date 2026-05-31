package com.zhou6.cloud.user.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 系统用户实体，对应 sys_user 表。
 */
@TableName("sys_user")
public class SysUser {

    /** 主键ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录账号。 */
    private String username;

    /** 登录密码。 */
    private String password;

    /** 用户昵称。 */
    private String nickname;

    /** 邮箱。 */
    private String email;

    /** 联系电话。 */
    private String contactPhone;

    /** 头像文件ID，对应 file-service 的 sys_file.id。 */
    private Long avatarFileId;

    /** 个性签名。 */
    private String personalSignature;

    /** 性别，0未知，1男，2女。 */
    private Short gender;

    /** 主部门ID，矩阵组织下的冗余查询字段。 */
    private Long primaryOrgId;

    /** 账号状态，1正常，0禁用。 */
    private Short status;

    /** 工作状态，取值可来自字典表。 */
    private String workStatus;

    /** 锁定状态，1已锁定，0未锁定。 */
    private Short isLocked;

    /** 连续登录失败次数。 */
    private Integer failedLoginAttempts;

    /** 账号锁定到期时间。 */
    private LocalDateTime lockedUntil;

    /** 最后登录IP。 */
    private String lastLoginIp;

    /** 最后登录时间。 */
    private LocalDateTime lastLoginTime;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Long getAvatarFileId() {
        return avatarFileId;
    }

    public void setAvatarFileId(Long avatarFileId) {
        this.avatarFileId = avatarFileId;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public Short getGender() {
        return gender;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public Long getPrimaryOrgId() {
        return primaryOrgId;
    }

    public void setPrimaryOrgId(Long primaryOrgId) {
        this.primaryOrgId = primaryOrgId;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public Short getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Short isLocked) {
        this.isLocked = isLocked;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
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
