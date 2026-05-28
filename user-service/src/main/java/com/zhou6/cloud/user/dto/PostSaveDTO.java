package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 岗位新增和修改请求参数。
 */
@Data
public class PostSaveDTO {

    /** 岗位ID，新增时为空，修改时必填。 */
    private String id;

    /** 岗位编码，新增时必填且全局唯一。 */
    private String postCode;

    /** 岗位名称。 */
    private String postName;

    /** 排序号。 */
    private Integer sortOrder = 0;
}
