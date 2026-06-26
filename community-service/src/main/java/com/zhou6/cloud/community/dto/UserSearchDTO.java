package com.zhou6.cloud.community.dto;

import lombok.Data;

@Data
public class UserSearchDTO {

    private String username;
    private String nickname;
    private Integer status = 1;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
