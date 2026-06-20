package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "岗位已分配用户分页查询请求参数")
public class PostConfigUserQueryDTO {

    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long postId;

    @Schema(description = "部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20001")
    private Long orgId;

    @Schema(description = "登录账号，支持右匹配查询", example = "zhang")
    private String username;

    @Schema(description = "用户昵称，支持右匹配查询", example = "张")
    private String nickname;

    @Schema(description = "当前页码，默认1", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认10", example = "10")
    private Integer pageSize = 10;
}
