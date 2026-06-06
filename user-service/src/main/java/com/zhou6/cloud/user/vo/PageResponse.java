package com.zhou6.cloud.user.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页响应对象。
 *
 * @param <T> 分页记录类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用分页响应对象")
public class PageResponse<T> {

    @Schema(description = "总记录数", example = "100")
    private String total;

    @Schema(description = "当前页码", example = "1")
    private String pageNum;

    @Schema(description = "每页数量", example = "10")
    private String pageSize;

    @Schema(description = "当前页记录列表")
    private List<T> records;

    public PageResponse(long total, long pageNum, long pageSize, List<T> records) {
        this.total = String.valueOf(total);
        this.pageNum = String.valueOf(pageNum);
        this.pageSize = String.valueOf(pageSize);
        this.records = records;
    }
}
