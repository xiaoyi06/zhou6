package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.dto.PageResponse;
import com.zhou6.cloud.user.dto.PostChangeStatusDTO;
import com.zhou6.cloud.user.dto.PostIdDTO;
import com.zhou6.cloud.user.dto.PostQueryDTO;
import com.zhou6.cloud.user.dto.PostSaveDTO;
import com.zhou6.cloud.user.dto.PostVO;
import com.zhou6.cloud.user.service.PostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位管理接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 分页查询岗位。
     *
     * @param dto 查询条件
     * @return 岗位分页结果
     */
    @PostMapping("/page")
    public R<PageResponse<PostVO>> page(@RequestBody PostQueryDTO dto) {
        return R.ok(postService.page(dto));
    }

    /**
     * 新增岗位。
     *
     * @param dto 岗位保存参数
     * @return 空响应
     */
    @PostMapping("/add")
    public R<Void> add(@RequestBody PostSaveDTO dto) {
        postService.add(dto);
        return R.ok(null);
    }

    /**
     * 修改岗位。
     *
     * @param dto 岗位保存参数
     * @return 空响应
     */
    @PostMapping("/edit")
    public R<Void> edit(@RequestBody PostSaveDTO dto) {
        postService.edit(dto);
        return R.ok(null);
    }

    /**
     * 删除岗位。
     *
     * @param dto 岗位 ID 参数
     * @return 空响应
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody PostIdDTO dto) {
        postService.delete(dto);
        return R.ok(null);
    }

    /**
     * 修改岗位启停状态。
     *
     * @param dto 状态参数
     * @return 空响应
     */
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody PostChangeStatusDTO dto) {
        postService.changeStatus(dto);
        return R.ok(null);
    }
}
