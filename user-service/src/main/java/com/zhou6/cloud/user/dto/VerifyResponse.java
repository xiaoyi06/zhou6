package com.zhou6.cloud.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResponse {

    private boolean verified;

    private String userId;

    private String username;

    private String nickname;

    private String email;

    private String contactPhone;

    private String message;
}
