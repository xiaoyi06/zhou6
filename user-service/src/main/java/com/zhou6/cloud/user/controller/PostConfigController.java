package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.constant.UserApiPathConstants;
import com.zhou6.cloud.user.dto.PostAssignDTO;
import com.zhou6.cloud.user.dto.PostRemoveUserDTO;
import com.zhou6.cloud.user.dto.PostUserQueryDTO;
import com.zhou6.cloud.user.vo.PostUserVO;
import com.zhou6.cloud.user.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位配置接口，负责维护用户-岗位-部门三元关系。
 */
@Tag(name = "岗位配置", description = "维护用户、岗位、组织机构三元关系")
@RestController
@RequestMapping(UserApiPathConstants.POST_CONFIG)
public class PostConfigController {

    private final PostService postService;

    public PostConfigController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 查询岗位下的人员。
     *
     * @param dto 查询参数
     * @return 岗位人员列表
     */
    @PostMapping("/users")
    @Operation(summary = "查询岗位人员", description = "查询岗位下的用户，可按组织机构过滤")
    public R<List<PostUserVO>> users(@RequestBody PostUserQueryDTO dto) {
        return R.ok(postService.users(dto));
    }

    /**
     * 批量为用户分配岗位。
     *
     * @param dto 岗位分配参数
     * @return 空响应
     */
    @PostMapping("/assignUsers")
    @Operation(summary = "分配用户岗位", description = "批量为用户分配指定组织机构范围内的岗位")
    public R<Void> assignUsers(@RequestBody PostAssignDTO dto) {
        postService.assignUsers(dto);
        return R.ok(null);
    }

    /**
     * 取消用户岗位。
     *
     * @param dto 取消岗位参数
     * @return 空响应
     */
    @PostMapping("/removeUser")
    @Operation(summary = "取消用户岗位", description = "移除用户在指定组织机构范围内的岗位关系")
    public R<Void> removeUser(@RequestBody PostRemoveUserDTO dto) {
        postService.removeUser(dto);
        return R.ok(null);
    }
}
