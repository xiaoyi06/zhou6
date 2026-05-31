package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 当前用户修改头像请求参数。
 */
@Data
public class UserAvatarDTO {

    /** 头像文件 ID，对应 file-service 上传后返回的 id。 */
    private String avatarFileId;
}
