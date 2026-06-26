package com.zhou6.cloud.community.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("community_user_tag_interest")
public class CommunityUserTagInterest {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long tagId;
    private Long browseCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private Integer interestScore;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
