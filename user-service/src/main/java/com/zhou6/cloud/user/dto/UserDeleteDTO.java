package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 用户删除请求参数，支持批量删除。
 */
@Data
public class UserDeleteDTO {

    /** 单个用户ID。 */
    private String id;

    /** 用户ID列表。 */
    private List<String> ids;
}
