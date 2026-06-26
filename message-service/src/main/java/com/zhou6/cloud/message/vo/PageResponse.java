package com.zhou6.cloud.message.vo;

import java.util.List;

/**
 * 消息中心分页响应。
 *
 * @param <T> 分页记录类型
 */
public class PageResponse<T> {

    private String total;
    private String pageNum;
    private String pageSize;
    private List<T> records;

    public PageResponse() {
    }

    public PageResponse(long total, long pageNum, long pageSize, List<T> records) {
        this.total = String.valueOf(total);
        this.pageNum = String.valueOf(pageNum);
        this.pageSize = String.valueOf(pageSize);
        this.records = records;
    }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }
    public String getPageNum() { return pageNum; }
    public void setPageNum(String pageNum) { this.pageNum = pageNum; }
    public String getPageSize() { return pageSize; }
    public void setPageSize(String pageSize) { this.pageSize = pageSize; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
