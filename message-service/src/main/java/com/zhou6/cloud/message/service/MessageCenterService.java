package com.zhou6.cloud.message.service;

import com.zhou6.cloud.message.dto.InternalMessageSendDTO;
import com.zhou6.cloud.message.dto.MessagePageQueryDTO;
import com.zhou6.cloud.message.dto.MessageReadAllDTO;
import com.zhou6.cloud.message.dto.MessageReadDTO;
import com.zhou6.cloud.message.dto.MessageUnreadCountDTO;
import com.zhou6.cloud.message.vo.MessageVO;
import com.zhou6.cloud.message.vo.PageResponse;

/**
 * 消息中心服务。
 */
public interface MessageCenterService {

    PageResponse<MessageVO> page(Long userId, MessagePageQueryDTO dto);

    long unreadCount(Long userId, MessageUnreadCountDTO dto);

    void markRead(Long userId, MessageReadDTO dto);

    void markAllRead(Long userId, MessageReadAllDTO dto);

    void sendInternalMessage(InternalMessageSendDTO dto);
}
