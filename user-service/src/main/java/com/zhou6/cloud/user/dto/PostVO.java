package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 岗位响应对象。
 */
@Data
public class PostVO {

    /** 岗位ID。 */
    private String id;

    /** 岗位编码。 */
    private String postCode;

    /** 岗位名称。 */
    private String postName;

    /** 排序号。 */
    private Integer sortOrder;

    /** 岗位状态，1正常，0停用。 */
    private Integer status;
}
