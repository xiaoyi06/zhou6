package com.zhou6.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户岗位关联实体，对应 sys_user_post 表。
 */
@TableName("sys_user_post")
public class SysUserPost {

    /** 用户ID。 */
    private Long userId;

    /** 岗位ID。 */
    private Long postId;

    /** 部门ID，用于矩阵架构下限定岗位生效范围。 */
    private Long orgId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
}
