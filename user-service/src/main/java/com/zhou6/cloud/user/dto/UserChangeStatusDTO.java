package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 用户启停请求参数。
 */
@Data
public class UserChangeStatusDTO {

    /** 用户ID。 */
    private String id;

    /** 账号状态，1正常，0禁用。 */
    private Integer status;
}
