package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.constant.UserApiPathConstants;
import com.zhou6.cloud.user.dto.PostAssignDTO;
import com.zhou6.cloud.user.dto.PostConfigUnassignedQueryDTO;
import com.zhou6.cloud.user.dto.PostConfigUserQueryDTO;
import com.zhou6.cloud.user.dto.PostRemoveUserDTO;
import com.zhou6.cloud.user.dto.PostUserQueryDTO;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.vo.PostConfigUserVO;
import com.zhou6.cloud.user.vo.PostUserVO;
import com.zhou6.cloud.user.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 分页查询岗位已分配用户。
     *
     * @param dto 查询参数
     * @return 已分配用户分页结果
     */
    @PostMapping("/usersPage")
    @Operation(summary = "分页查询岗位已分配用户", description = "分页查询指定岗位+部门下已分配的用户，支持用户名和昵称过滤")
    public R<PageResponse<PostConfigUserVO>> usersPage(@RequestBody PostConfigUserQueryDTO dto) {
        return R.ok(postService.usersPage(dto));
    }

    /**
     * 分页查询未分配岗位的用户。
     *
     * @param dto 查询参数
     * @return 未分配用户分页结果
     */
    @PostMapping("/unassignedUsers")
    @Operation(summary = "查询未分配岗位用户", description = "分页查询尚未分配到指定岗位+部门的用户，支持用户名和昵称过滤")
    public R<PageResponse<PostConfigUserVO>> unassignedUsers(@RequestBody PostConfigUnassignedQueryDTO dto) {
        return R.ok(postService.unassignedUsers(dto));
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
    @Operation(summary = "取消用户岗位", description = "批量移除用户在指定组织机构范围内的岗位关系")
    public R<Void> removeUser(@RequestBody PostRemoveUserDTO dto) {
        postService.removeUser(dto);
        return R.ok(null);
    }
}
