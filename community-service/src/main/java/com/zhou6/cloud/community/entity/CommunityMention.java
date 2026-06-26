package com.zhou6.cloud.community.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("community_mention")
public class CommunityMention {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String businessType;
    private Long businessId;
    private Long postId;
    private Long operatorUserId;
    private Long mentionedUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
