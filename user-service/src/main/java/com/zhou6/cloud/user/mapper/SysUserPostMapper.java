package com.zhou6.cloud.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.user.entity.SysUserPost;
import org.apache.ibatis.annotations.Param;

/**
 * 用户岗位关联 Mapper。
 */
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {

    int insertBatch(@Param("relations") List<SysUserPost> relations);
}
