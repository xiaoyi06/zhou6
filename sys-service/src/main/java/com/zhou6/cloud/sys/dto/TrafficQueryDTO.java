package com.zhou6.cloud.sys.dto;

/**
 * 流量统计查询参数。
 */
public class TrafficQueryDTO extends PageQueryDTO {

    private String apiRoute;
    private String beginTime;
    private String endTime;

    public String getApiRoute() { return apiRoute; }
    public void setApiRoute(String apiRoute) { this.apiRoute = apiRoute; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
