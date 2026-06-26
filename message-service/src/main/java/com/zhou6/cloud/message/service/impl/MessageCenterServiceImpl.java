package com.zhou6.cloud.message.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou6.cloud.message.dto.InternalMessageSendDTO;
import com.zhou6.cloud.message.dto.MessagePageQueryDTO;
import com.zhou6.cloud.message.dto.MessageReadAllDTO;
import com.zhou6.cloud.message.dto.MessageReadDTO;
import com.zhou6.cloud.message.dto.MessageUnreadCountDTO;
import com.zhou6.cloud.message.entity.MsgMessage;
import com.zhou6.cloud.message.entity.MsgMessageReceiver;
import com.zhou6.cloud.message.mapper.MsgMessageMapper;
import com.zhou6.cloud.message.mapper.MsgMessageReceiverMapper;
import com.zhou6.cloud.message.service.MessageCenterService;
import com.zhou6.cloud.message.vo.MessagePushVO;
import com.zhou6.cloud.message.vo.MessageVO;
import com.zhou6.cloud.message.vo.PageResponse;
import com.zhou6.cloud.message.websocket.MessageWebSocketPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息中心服务实现。
 */
@Service
public class MessageCenterServiceImpl extends BaseMessageService implements MessageCenterService {

    private static final String CHANNEL_INTERNAL = "INTERNAL";
    private static final String CHANNEL_EXTERNAL = "EXTERNAL";
    private static final String READ_UNREAD = "UNREAD";
    private static final String READ_READ = "READ";
    private static final String PUSH_PENDING = "PENDING";
    private static final String PUSH_SENT = "SENT";
    private static final String PUSH_FAILED = "FAILED";
    private static final String LINK_NONE = "NONE";
    private static final String UNREAD_ALL = "ALL";
    private static final String UNREAD_COUNT_PREFIX = "zhou6:message:unread:";
    private static final Duration UNREAD_COUNT_TTL = Duration.ofHours(6);

    private final MsgMessageMapper messageMapper;
    private final MsgMessageReceiverMapper receiverMapper;
    private final MessageWebSocketPublisher webSocketPublisher;
    private final StringRedisTemplate redisTemplate;

    public MessageCenterServiceImpl(MsgMessageMapper messageMapper, MsgMessageReceiverMapper receiverMapper,
            MessageWebSocketPublisher webSocketPublisher, StringRedisTemplate redisTemplate) {
        this.messageMapper = messageMapper;
        this.receiverMapper = receiverMapper;
        this.webSocketPublisher = webSocketPublisher;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public PageResponse<MessageVO> page(Long userId, MessagePageQueryDTO dto) {
        requireCurrentUser(userId);
        MessagePageQueryDTO query = dto == null ? new MessagePageQueryDTO() : dto;
        validateQuery(query);
        String channel = normalizeNullableChannel(query.getChannel());
        String messageType = normalizeNullableValue(query.getMessageType());
        String readStatus = normalizeNullableReadStatus(query.getReadStatus());
        Page<MessageVO> page = receiverMapper.pageUserMessages(Page.of(pageNum(query.getPageNum()),
                        pageSize(query.getPageSize())), userId, channel, messageType, readStatus,
                query.getBeginTime(), query.getEndTime());
        page.getRecords().forEach(this::fillDisplayFields);
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public long unreadCount(Long userId, MessageUnreadCountDTO dto) {
        requireCurrentUser(userId);
        String channel = dto == null ? null : normalizeNullableChannel(dto.getChannel());
        Long cached = cachedUnreadCount(userId, channel);
        if (cached != null) {
            return cached;
        }
        Long count = receiverMapper.countUnread(userId, channel);
        long value = count == null ? 0L : count;
        cacheUnreadCount(userId, channel, value);
        return value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, MessageReadDTO dto) {
        requireCurrentUser(userId);
        require(dto != null && dto.getIds() != null && !dto.getIds().isEmpty(), "消息ID不能为空");
        List<Long> ids = dto.getIds().stream()
                .map(id -> parseRequiredId(id, "消息ID不正确"))
                .distinct()
                .toList();
        if (receiverMapper.markRead(userId, ids, LocalDateTime.now()) > 0) {
            evictUnreadCounts(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId, MessageReadAllDTO dto) {
        requireCurrentUser(userId);
        String channel = dto == null ? null : normalizeNullableChannel(dto.getChannel());
        String messageType = dto == null ? null : normalizeNullableValue(dto.getMessageType());
        if (receiverMapper.markAllRead(userId, channel, messageType, LocalDateTime.now()) > 0) {
            evictUnreadCounts(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendInternalMessage(InternalMessageSendDTO dto) {
        validateSend(dto);
        String channel = normalizeChannel(dto.getChannel());
        MsgMessage message = findExistingMessage(channel, dto);
        if (message == null) {
            message = buildMessage(channel, dto);
            messageMapper.insert(message);
        }
        List<Long> receiverUserIds = dto.getReceiverUserIds().stream()
                .map(id -> parseRequiredId(id, "接收人ID不正确"))
                .distinct()
                .toList();
        for (Long receiverUserId : receiverUserIds) {
            MsgMessageReceiver receiver = findExistingReceiver(message.getId(), receiverUserId);
            if (receiver == null) {
                receiver = new MsgMessageReceiver();
                receiver.setMessageId(message.getId());
                receiver.setUserId(receiverUserId);
                receiver.setReadStatus(READ_UNREAD);
                receiver.setPushStatus(PUSH_PENDING);
                receiverMapper.insert(receiver);
                incrementUnreadCounts(receiverUserId, channel);
            }
            pushMessage(receiver, message);
        }
    }

    private void pushMessage(MsgMessageReceiver receiver, MsgMessage message) {
        MessageVO vo = toVo(receiver, message);
        MessagePushVO push = new MessagePushVO();
        push.setEventType("MESSAGE_CREATED");
        push.setUnreadCount(String.valueOf(unreadCount(receiver.getUserId(), unreadCountDto(message.getChannel()))));
        push.setMessage(vo);
        try {
            webSocketPublisher.pushToUser(String.valueOf(receiver.getUserId()), push);
            receiverMapper.updatePushStatus(receiver.getId(), PUSH_SENT, LocalDateTime.now());
        } catch (Exception ex) {
            receiverMapper.updatePushStatus(receiver.getId(), PUSH_FAILED, LocalDateTime.now());
        }
    }

    private Long cachedUnreadCount(Long userId, String channel) {
        try {
            String value = redisTemplate.opsForValue().get(unreadCountKey(userId, channel));
            return value == null ? null : Long.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private void cacheUnreadCount(Long userId, String channel, long count) {
        try {
            redisTemplate.opsForValue().set(unreadCountKey(userId, channel), String.valueOf(Math.max(0L, count)),
                    UNREAD_COUNT_TTL);
        } catch (Exception ignored) {
            // 未读数缓存不可用时降级为数据库计数。
        }
    }

    private void incrementUnreadCounts(Long userId, String channel) {
        try {
            incrementUnreadCount(userId, channel);
            incrementUnreadCount(userId, null);
        } catch (Exception ignored) {
            evictUnreadCounts(userId);
        }
    }

    private void incrementUnreadCount(Long userId, String channel) {
        String key = unreadCountKey(userId, channel);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, UNREAD_COUNT_TTL);
    }

    private void evictUnreadCounts(Long userId) {
        try {
            redisTemplate.delete(List.of(
                    unreadCountKey(userId, null),
                    unreadCountKey(userId, CHANNEL_INTERNAL),
                    unreadCountKey(userId, CHANNEL_EXTERNAL)));
        } catch (Exception ignored) {
            // 删除失败时等待 TTL 兜底，下一次 DB 查询仍可回填。
        }
    }

    private String unreadCountKey(Long userId, String channel) {
        return UNREAD_COUNT_PREFIX + userId + ":" + (hasText(channel) ? channel : UNREAD_ALL);
    }

    private MessageUnreadCountDTO unreadCountDto(String channel) {
        MessageUnreadCountDTO dto = new MessageUnreadCountDTO();
        dto.setChannel(channel);
        return dto;
    }

    private MsgMessage findExistingMessage(String channel, InternalMessageSendDTO dto) {
        if (!hasText(dto.getSourceType()) || !hasText(dto.getSourceId())) {
            return null;
        }
        return messageMapper.selectOne(new LambdaQueryWrapper<MsgMessage>()
                .eq(MsgMessage::getChannel, channel)
                .eq(MsgMessage::getSourceType, normalizeNullableValue(dto.getSourceType()))
                .eq(MsgMessage::getSourceId, dto.getSourceId().trim())
                .last("limit 1"));
    }

    private MsgMessageReceiver findExistingReceiver(Long messageId, Long userId) {
        return receiverMapper.selectOne(new LambdaQueryWrapper<MsgMessageReceiver>()
                .eq(MsgMessageReceiver::getMessageId, messageId)
                .eq(MsgMessageReceiver::getUserId, userId)
                .last("limit 1"));
    }

    private MsgMessage buildMessage(String channel, InternalMessageSendDTO dto) {
        MsgMessage message = new MsgMessage();
        message.setChannel(channel);
        message.setMessageType(normalizeRequiredValue(dto.getMessageType(), "消息类型不能为空"));
        message.setTitle(dto.getTitle().trim());
        message.setContent(hasText(dto.getContent()) ? dto.getContent().trim() : null);
        message.setSourceType(normalizeNullableValue(dto.getSourceType()));
        message.setSourceName(hasText(dto.getSourceName()) ? dto.getSourceName().trim() : null);
        message.setSourceId(hasText(dto.getSourceId()) ? dto.getSourceId().trim() : null);
        message.setBusinessType(normalizeNullableValue(dto.getBusinessType()));
        message.setBusinessId(hasText(dto.getBusinessId()) ? dto.getBusinessId().trim() : null);
        message.setLinkType(hasText(dto.getLinkType()) ? dto.getLinkType().trim().toUpperCase() : LINK_NONE);
        message.setLinkUrl(hasText(dto.getLinkUrl()) ? dto.getLinkUrl().trim() : null);
        message.setLinkParams(hasText(dto.getLinkParams()) ? dto.getLinkParams().trim() : null);
        message.setSendTime(LocalDateTime.now());
        return message;
    }

    private MessageVO toVo(MsgMessageReceiver receiver, MsgMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(String.valueOf(receiver.getId()));
        vo.setMessageId(String.valueOf(message.getId()));
        vo.setChannel(message.getChannel());
        vo.setMessageType(message.getMessageType());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setSourceType(message.getSourceType());
        vo.setSourceName(message.getSourceName());
        vo.setSourceId(message.getSourceId());
        vo.setBusinessType(message.getBusinessType());
        vo.setBusinessId(message.getBusinessId());
        vo.setReadStatus(receiver.getReadStatus());
        vo.setSendTime(message.getSendTime());
        vo.setReadTime(receiver.getReadTime());
        vo.setLinkType(message.getLinkType());
        vo.setLinkUrl(message.getLinkUrl());
        vo.setLinkParams(message.getLinkParams());
        fillDisplayFields(vo);
        return vo;
    }

    private void fillDisplayFields(MessageVO vo) {
        vo.setMessageTypeName(typeName(vo.getMessageType()));
        if (!hasText(vo.getSourceName())) {
            vo.setSourceName(typeName(vo.getSourceType()));
        }
    }

    private String typeName(String type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case "APPROVAL" -> "审批";
            case "WORKFLOW" -> "流程";
            case "TODO" -> "待办";
            case "PERMISSION" -> "权限";
            case "ACCOUNT" -> "账号";
            case "PASSWORD" -> "密码";
            case "SECURITY" -> "安全";
            case "SYSTEM" -> "系统";
            case "EMAIL" -> "邮件";
            case "API" -> "接口";
            case "ALERT" -> "告警";
            case "EXTERNAL_SYSTEM" -> "外部系统";
            default -> type;
        };
    }

    private void validateSend(InternalMessageSendDTO dto) {
        require(dto != null, "消息参数不能为空");
        normalizeChannel(dto.getChannel());
        normalizeRequiredValue(dto.getMessageType(), "消息类型不能为空");
        require(hasText(dto.getTitle()), "消息标题不能为空");
        require(dto.getTitle().trim().length() <= 200, "消息标题不能超过200个字符");
        require(dto.getContent() == null || dto.getContent().trim().length() <= 2000, "消息内容不能超过2000个字符");
        require(dto.getReceiverUserIds() != null && !dto.getReceiverUserIds().isEmpty(), "接收人不能为空");
    }

    private void validateQuery(MessagePageQueryDTO query) {
        normalizeNullableChannel(query.getChannel());
        normalizeNullableReadStatus(query.getReadStatus());
        require(query.getBeginTime() == null || query.getEndTime() == null
                || !query.getBeginTime().isAfter(query.getEndTime()), "开始时间不能晚于结束时间");
    }

    private String normalizeChannel(String channel) {
        String value = hasText(channel) ? channel.trim().toUpperCase() : CHANNEL_INTERNAL;
        require(CHANNEL_INTERNAL.equals(value) || CHANNEL_EXTERNAL.equals(value), "消息通道不正确");
        return value;
    }

    private String normalizeNullableChannel(String channel) {
        if (!hasText(channel)) {
            return null;
        }
        return normalizeChannel(channel);
    }

    private String normalizeNullableReadStatus(String readStatus) {
        if (!hasText(readStatus)) {
            return null;
        }
        String value = readStatus.trim().toUpperCase();
        require(READ_UNREAD.equals(value) || READ_READ.equals(value), "消息阅读状态不正确");
        return value;
    }

    private String normalizeRequiredValue(String value, String message) {
        require(hasText(value), message);
        return value.trim().toUpperCase();
    }

    private String normalizeNullableValue(String value) {
        return hasText(value) ? value.trim().toUpperCase() : null;
    }

    private void requireCurrentUser(Long userId) {
        require(userId != null && userId > 0, "当前用户不存在");
    }
}
