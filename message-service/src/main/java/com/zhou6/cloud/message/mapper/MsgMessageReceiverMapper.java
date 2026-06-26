package com.zhou6.cloud.message.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.message.entity.MsgMessageReceiver;
import com.zhou6.cloud.message.vo.MessageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息接收人 Mapper。
 */
public interface MsgMessageReceiverMapper extends BaseMapper<MsgMessageReceiver> {

    @Select("<script>"
            + "SELECT r.id AS id, m.id AS message_id, m.channel AS channel, m.message_type AS message_type, "
            + "m.title AS title, m.content AS content, m.source_type AS source_type, m.source_name AS source_name, "
            + "m.source_id AS source_id, m.business_type AS business_type, m.business_id AS business_id, "
            + "r.read_status AS read_status, m.send_time AS send_time, r.read_time AS read_time, "
            + "m.link_type AS link_type, m.link_url AS link_url, m.link_params AS link_params "
            + "FROM msg_message_receiver r JOIN msg_message m ON r.message_id = m.id "
            + "WHERE r.user_id = #{userId} "
            + "<if test='channel != null and channel != \"\"'>AND m.channel = #{channel} </if>"
            + "<if test='messageType != null and messageType != \"\"'>AND m.message_type = #{messageType} </if>"
            + "<if test='readStatus != null and readStatus != \"\"'>AND r.read_status = #{readStatus} </if>"
            + "<if test='beginTime != null'>AND m.send_time &gt;= #{beginTime} </if>"
            + "<if test='endTime != null'>AND m.send_time &lt;= #{endTime} </if>"
            + "ORDER BY m.send_time DESC, r.id DESC"
            + "</script>")
    Page<MessageVO> pageUserMessages(Page<MessageVO> page, @Param("userId") Long userId,
            @Param("channel") String channel, @Param("messageType") String messageType,
            @Param("readStatus") String readStatus, @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("<script>"
            + "SELECT COUNT(1) FROM msg_message_receiver r JOIN msg_message m ON r.message_id = m.id "
            + "WHERE r.user_id = #{userId} AND r.read_status = 'UNREAD' "
            + "<if test='channel != null and channel != \"\"'>AND m.channel = #{channel} </if>"
            + "</script>")
    Long countUnread(@Param("userId") Long userId, @Param("channel") String channel);

    @Update("<script>"
            + "UPDATE msg_message_receiver SET read_status = 'READ', read_time = #{now}, update_time = #{now} "
            + "WHERE user_id = #{userId} AND read_status = 'UNREAD' AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    int markRead(@Param("userId") Long userId, @Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

    @Update("<script>"
            + "UPDATE msg_message_receiver r SET read_status = 'READ', read_time = #{now}, update_time = #{now} "
            + "FROM msg_message m WHERE r.message_id = m.id AND r.user_id = #{userId} "
            + "AND r.read_status = 'UNREAD' "
            + "<if test='channel != null and channel != \"\"'>AND m.channel = #{channel} </if>"
            + "<if test='messageType != null and messageType != \"\"'>AND m.message_type = #{messageType} </if>"
            + "</script>")
    int markAllRead(@Param("userId") Long userId, @Param("channel") String channel,
            @Param("messageType") String messageType, @Param("now") LocalDateTime now);

    @Update("UPDATE msg_message_receiver SET push_status = #{pushStatus}, push_time = #{now}, update_time = #{now} "
            + "WHERE id = #{id}")
    int updatePushStatus(@Param("id") Long id, @Param("pushStatus") String pushStatus,
            @Param("now") LocalDateTime now);
}
