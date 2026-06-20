package com.zhou6.cloud.workflow.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页响应对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private String total;

    private String pageNum;

    private String pageSize;

    private List<T> records;

    public PageResponse(long total, long pageNum, long pageSize, List<T> records) {
        this.total = String.valueOf(total);
        this.pageNum = String.valueOf(pageNum);
        this.pageSize = String.valueOf(pageSize);
        this.records = records;
    }
}
