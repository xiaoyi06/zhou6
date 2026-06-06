package com.zhou6.cloud.user.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.user.constant.UserApiPathConstants;
import com.zhou6.cloud.user.vo.PageResponse;
import com.zhou6.cloud.user.dto.PostChangeStatusDTO;
import com.zhou6.cloud.user.dto.PostIdDTO;
import com.zhou6.cloud.user.dto.PostQueryDTO;
import com.zhou6.cloud.user.dto.PostSaveDTO;
import com.zhou6.cloud.user.vo.PostVO;
import com.zhou6.cloud.user.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位管理接口，统一采用 POST + JSON Body 的 RPC 风格路由。
 */
@Tag(name = "岗位管理", description = "维护岗位档案和岗位启停状态")
@RestController
@RequestMapping(UserApiPathConstants.POST)
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
    @Operation(summary = "分页查询岗位", description = "按岗位编码、名称或状态分页查询岗位")
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
    @Operation(summary = "新增岗位", description = "新增岗位档案")
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
    @Operation(summary = "修改岗位", description = "修改岗位名称、排序或状态等基础信息")
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
    @Operation(summary = "删除岗位", description = "删除岗位；岗位已分配给用户时不允许删除")
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
    @Operation(summary = "修改岗位状态", description = "启用或停用指定岗位")
    public R<Void> changeStatus(@RequestBody PostChangeStatusDTO dto) {
        postService.changeStatus(dto);
        return R.ok(null);
    }
}
