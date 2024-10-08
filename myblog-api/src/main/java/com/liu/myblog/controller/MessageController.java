package com.liu.myblog.controller;

import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Message;
import com.liu.myblog.dao.dto.MessageDto;
import com.liu.myblog.rabbitmq.MessageProducer;
import com.liu.myblog.service.MessageService;
import com.liu.myblog.util.TokenUtil;
import com.liu.myblog.websocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private MessageService messageService;
    @Autowired
    private WebSocketHandler webSocketHandler;

    @PostMapping
    public ReturnData sendMessage(@RequestBody Message message, HttpServletRequest request) {
        message.setSenderId(tokenUtil.getUserId(request)).setCreateTime(new Date());
        messageProducer.sendMessage(message);
        return ReturnData.success(message);
    }

    @GetMapping("/unread")
    public ReturnData getUnreadNotificationNum(HttpServletRequest request) {
        Long userId = tokenUtil.getUserId(request);
        return ReturnData.success(messageService.getUnreadMessageNum(userId));
    }

    @GetMapping
    public ReturnData getMessages(HttpServletRequest request,
                                  @RequestParam("contactId") long contactId,
                                  @RequestParam("start") int start,
                                  @RequestParam(defaultValue = "50") int limit) {
        Long userId = tokenUtil.getUserId(request);
        List<MessageDto> recentMessages = messageService.getMessages(userId, contactId, start, limit);
        webSocketHandler.refreshMessageNum(userId);
        return ReturnData.success(recentMessages);
    }

    @PostMapping("/refresh")
    public ReturnData refreshUnreadMessage(@RequestParam("contactId") long contactId,
                                           HttpServletRequest request) {
        Long userId = tokenUtil.getUserId(request);
        messageService.refreshUnreadMessage(userId, contactId);
        webSocketHandler.refreshMessageNum(userId);
        return ReturnData.success();
    }

}
