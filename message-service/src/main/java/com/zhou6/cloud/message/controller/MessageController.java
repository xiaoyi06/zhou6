package com.zhou6.cloud.message.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.message.constant.MessageApiPathConstants;
import com.zhou6.cloud.message.dto.MessagePageQueryDTO;
import com.zhou6.cloud.message.dto.MessageReadAllDTO;
import com.zhou6.cloud.message.dto.MessageReadDTO;
import com.zhou6.cloud.message.dto.MessageUnreadCountDTO;
import com.zhou6.cloud.message.service.MessageCenterService;
import com.zhou6.cloud.message.vo.MessageVO;
import com.zhou6.cloud.message.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "消息中心", description = "查询站内/站外通知、未读数量和已读状态")
@RestController
@RequestMapping(MessageApiPathConstants.MESSAGE)
public class MessageController {

    private final MessageCenterService messageService;

    public MessageController(MessageCenterService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询我的消息")
    public R<PageResponse<MessageVO>> page(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody(required = false) MessagePageQueryDTO dto) {
        return R.ok(messageService.page(currentUserId(userId), dto));
    }

    @PostMapping("/unreadCount")
    @Operation(summary = "查询我的未读消息数量")
    public R<String> unreadCount(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody(required = false) MessageUnreadCountDTO dto) {
        return R.ok(String.valueOf(messageService.unreadCount(currentUserId(userId), dto)));
    }

    @PostMapping("/read")
    @Operation(summary = "批量标记消息已读")
    public R<Void> read(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody MessageReadDTO dto) {
        messageService.markRead(currentUserId(userId), dto);
        return R.ok(null);
    }

    @PostMapping("/readAll")
    @Operation(summary = "按通道或类型全部已读")
    public R<Void> readAll(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody(required = false) MessageReadAllDTO dto) {
        messageService.markAllRead(currentUserId(userId), dto);
        return R.ok(null);
    }

    private Long currentUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "缺少当前用户信息");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "当前用户信息不正确");
        }
    }
}
