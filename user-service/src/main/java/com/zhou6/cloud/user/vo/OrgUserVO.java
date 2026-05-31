package com.zhou6.cloud.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构人员响应对象。
 */
@Data
@Schema(description = "组织机构人员响应对象")
public class OrgUserVO {

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "登录账号", example = "zhangsan")
    private String username;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "联系电话", example = "13800000000")
    private String contactPhone;

    @Schema(description = "是否主部门：1是，0否", example = "1")
    private Short isPrimary;
}
