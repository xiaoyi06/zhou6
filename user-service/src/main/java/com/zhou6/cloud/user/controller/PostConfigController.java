package com.zhou6.cloud.user.controller;

import java.util.List;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.PostAssignDTO;
import com.zhou6.cloud.user.dto.PostRemoveUserDTO;
import com.zhou6.cloud.user.dto.PostUserQueryDTO;
import com.zhou6.cloud.user.dto.PostUserVO;
import com.zhou6.cloud.user.service.PostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位配置接口，负责维护用户-岗位-部门三元关系。
 */
@RestController
@RequestMapping("/api/v1/post/config")
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
    public R<Void> removeUser(@RequestBody PostRemoveUserDTO dto) {
        postService.removeUser(dto);
        return R.ok(null);
    }
}
