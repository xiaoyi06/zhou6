package com.zhou6.cloud.sys.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.sys.entity.SysTodo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysTodoMapper extends BaseMapper<SysTodo> {

    @Update("UPDATE sys_todo SET remind_status = 'SENT', notified_at = #{now}, update_time = #{now} "
            + "WHERE id = #{id} AND status = 'TODO' AND remind_status = 'PENDING'")
    int claimReminder(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Update("<script>UPDATE sys_todo SET remind_status = 'READ', read_at = #{now}, update_time = #{now} "
            + "WHERE user_id = #{userId} AND remind_status = 'SENT' AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int markRemindersRead(@Param("userId") Long userId, @Param("ids") List<Long> ids,
            @Param("now") LocalDateTime now);
}
