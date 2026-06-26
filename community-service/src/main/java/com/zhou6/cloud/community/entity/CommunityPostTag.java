package com.zhou6.cloud.community.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("community_post_tag")
public class CommunityPostTag {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long postId;
    private Long tagId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
