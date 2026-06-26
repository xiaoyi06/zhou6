package com.zhou6.cloud.community.vo;

import java.util.List;

import lombok.Data;

@Data
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
}
