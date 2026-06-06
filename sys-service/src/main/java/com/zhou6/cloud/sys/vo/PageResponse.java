package com.zhou6.cloud.sys.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统服务通用分页响应对象。
 *
 * @param <T> 分页记录类型
 */
@Schema(description = "系统服务通用分页响应对象")
public class PageResponse<T> {

    @Schema(description = "总记录数", example = "100")
    private String total;

    @Schema(description = "当前页码", example = "1")
    private String pageNum;

    @Schema(description = "每页数量", example = "10")
    private String pageSize;

    @Schema(description = "当前页记录列表")
    private List<T> records;

    public PageResponse() {
    }

    public PageResponse(long total, long pageNum, long pageSize, List<T> records) {
        this.total = String.valueOf(total);
        this.pageNum = String.valueOf(pageNum);
        this.pageSize = String.valueOf(pageSize);
        this.records = records;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPageNum() {
        return pageNum;
    }

    public void setPageNum(String pageNum) {
        this.pageNum = pageNum;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
