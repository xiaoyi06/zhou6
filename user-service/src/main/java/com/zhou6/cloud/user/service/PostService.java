package com.zhou6.cloud.user.service;

import java.util.List;

import com.zhou6.cloud.user.dto.PageResponse;
import com.zhou6.cloud.user.dto.PostAssignDTO;
import com.zhou6.cloud.user.dto.PostChangeStatusDTO;
import com.zhou6.cloud.user.dto.PostIdDTO;
import com.zhou6.cloud.user.dto.PostQueryDTO;
import com.zhou6.cloud.user.dto.PostRemoveUserDTO;
import com.zhou6.cloud.user.dto.PostSaveDTO;
import com.zhou6.cloud.user.dto.PostUserQueryDTO;
import com.zhou6.cloud.user.dto.PostUserVO;
import com.zhou6.cloud.user.dto.PostVO;

/**
 * 岗位管理业务接口，负责岗位基础维护和矩阵岗位配置。
 */
public interface PostService {

    /**
     * 分页查询岗位。
     *
     * @param dto 查询条件
     * @return 岗位分页结果
     */
    PageResponse<PostVO> page(PostQueryDTO dto);

    /**
     * 新增岗位。
     *
     * @param dto 岗位保存参数
     */
    void add(PostSaveDTO dto);

    /**
     * 修改岗位；岗位编码不允许修改。
     *
     * @param dto 岗位保存参数
     */
    void edit(PostSaveDTO dto);

    /**
     * 删除岗位。
     *
     * @param dto 岗位 ID 参数
     */
    void delete(PostIdDTO dto);

    /**
     * 修改岗位启停状态。
     *
     * @param dto 状态参数
     */
    void changeStatus(PostChangeStatusDTO dto);

    /**
     * 查询岗位下的人员。
     *
     * @param dto 查询参数
     * @return 岗位用户列表
     */
    List<PostUserVO> users(PostUserQueryDTO dto);

    /**
     * 批量为用户分配岗位。
     *
     * @param dto 岗位分配参数
     */
    void assignUsers(PostAssignDTO dto);

    /**
     * 取消用户岗位。
     *
     * @param dto 取消岗位参数
     */
    void removeUser(PostRemoveUserDTO dto);
}
