package com.zhou6.cloud.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private String userId;

    private String username;

    private String nickname;

    private String email;

    private String contactPhone;

    /** 头像文件 ID，对应 file-service 的 sys_file.id。 */
    private String avatarFileId;

    /** 头像访问地址。 */
    private String avatarUrl;

    public UserInfoResponse(String userId, String username, String nickname, String email, String contactPhone) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.contactPhone = contactPhone;
    }
}
