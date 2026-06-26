package com.zhou6.cloud.message.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.message.constant.MessageApiPathConstants;
import com.zhou6.cloud.message.dto.InternalMessageSendDTO;
import com.zhou6.cloud.message.service.MessageCenterService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息中心内部接口，仅允许服务内网调用，不通过网关暴露给浏览器。
 */
@Hidden
@RestController
@RequestMapping(MessageApiPathConstants.MESSAGE_INNER)
public class MessageInnerController {

    private final MessageCenterService messageService;

    public MessageInnerController(MessageCenterService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public R<Void> send(@RequestBody InternalMessageSendDTO dto) {
        messageService.sendInternalMessage(dto);
        return R.ok(null);
    }
}
