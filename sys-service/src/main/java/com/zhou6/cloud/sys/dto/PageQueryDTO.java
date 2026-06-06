package com.zhou6.cloud.sys.dto;

/**
 * 分页查询基础参数。
 */
public class PageQueryDTO {

    private Integer pageNum;
    private Integer pageSize;

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
