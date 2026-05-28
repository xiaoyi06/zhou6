package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 部门人员响应对象。
 */
@Data
public class OrgUserVO {

    private Long userId;

    private String username;

    private String nickname;

    private String email;

    private String contactPhone;

    private Short isPrimary;
}
