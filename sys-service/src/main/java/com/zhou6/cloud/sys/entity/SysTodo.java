package com.zhou6.cloud.sys.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 统一待办实体，既承载用户手工待办，也承载流程生成的待办。
 */
@TableName("sys_todo")
public class SysTodo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private LocalDateTime remindTime;
    private String status;
    private String remindStatus;
    private LocalDateTime notifiedAt;
    private LocalDateTime readAt;
    private String sourceType;
    private String sourceId;
    private String sourceBusinessType;
    private String sourceBusinessId;
    private String completeMode;
    private LocalDateTime finishTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getRemindTime() { return remindTime; }
    public void setRemindTime(LocalDateTime remindTime) { this.remindTime = remindTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public LocalDateTime getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(LocalDateTime notifiedAt) { this.notifiedAt = notifiedAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getSourceBusinessType() { return sourceBusinessType; }
    public void setSourceBusinessType(String sourceBusinessType) { this.sourceBusinessType = sourceBusinessType; }
    public String getSourceBusinessId() { return sourceBusinessId; }
    public void setSourceBusinessId(String sourceBusinessId) { this.sourceBusinessId = sourceBusinessId; }
    public String getCompleteMode() { return completeMode; }
    public void setCompleteMode(String completeMode) { this.completeMode = completeMode; }
    public LocalDateTime getFinishTime() { return finishTime; }
    public void setFinishTime(LocalDateTime finishTime) { this.finishTime = finishTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
