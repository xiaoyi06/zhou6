package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 岗位分页查询请求参数。
 */
@Data
public class PostQueryDTO {

    /** 岗位编码，支持模糊查询。 */
    private String postCode;

    /** 岗位名称，支持模糊查询。 */
    private String postName;

    /** 岗位状态，1正常，0停用。 */
    private Integer status;

    /** 当前页码。 */
    private Integer pageNum = 1;

    /** 每页条数。 */
    private Integer pageSize = 10;
}
