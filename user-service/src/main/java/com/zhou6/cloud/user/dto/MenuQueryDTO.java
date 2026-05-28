package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 菜单树查询请求参数。
 */
@Data
public class MenuQueryDTO {

    /** 菜单状态，1正常，0停用。 */
    private Integer status;
}
