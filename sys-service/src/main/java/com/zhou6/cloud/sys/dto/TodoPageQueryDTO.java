package com.zhou6.cloud.sys.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/** 我的待办分页查询参数。 */
public class TodoPageQueryDTO extends PageQueryDTO {

    private String status;
    private String timeType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTimeType() { return timeType; }
    public void setTimeType(String timeType) { this.timeType = timeType; }
    public LocalDateTime getBeginTime() { return beginTime; }
    public void setBeginTime(LocalDateTime beginTime) { this.beginTime = beginTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
