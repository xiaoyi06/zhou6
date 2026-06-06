package com.zhou6.cloud.sys.entity;

import java.time.LocalDateTime;

/**
 * 流量监控统计实体，对应 sys_traffic_stat 表。
 */
public class SysTrafficStat {

    private String apiRoute;
    private Long pv;
    private Long uv;
    private Integer avgRt;
    private LocalDateTime statTime;

    public String getApiRoute() { return apiRoute; }
    public void setApiRoute(String apiRoute) { this.apiRoute = apiRoute; }
    public Long getPv() { return pv; }
    public void setPv(Long pv) { this.pv = pv; }
    public Long getUv() { return uv; }
    public void setUv(Long uv) { this.uv = uv; }
    public Integer getAvgRt() { return avgRt; }
    public void setAvgRt(Integer avgRt) { this.avgRt = avgRt; }
    public LocalDateTime getStatTime() { return statTime; }
    public void setStatTime(LocalDateTime statTime) { this.statTime = statTime; }
}
