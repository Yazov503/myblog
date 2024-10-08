package com.liu.myblog.websocket;

import com.liu.myblog.dao.dto.MessageDto;
import com.liu.myblog.dao.dto.NotificationDto;
import com.liu.myblog.service.MessageService;
import com.liu.myblog.service.NotificationService;
import lombok.SneakyThrows;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private NotificationService notificationService;

    @Resource
    private MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public void sendNotification(NotificationDto notificationDto) {
        Long userId = notificationDto.getReceiverId();
        messagingTemplate.convertAndSend("/topic/notification/"+userId,
                objectMapper.writeValueAsString(notificationDto));
    }

    public void refreshNotificationNum(Long userId) {
        messagingTemplate.convertAndSend("/topic/notification/unread/"+userId,
                notificationService.getUnreadNotificationNum(userId));
    }

    @SneakyThrows
    public void sendMessage(MessageDto messageDto) {
        Long userId = messageDto.getReceiverId();
        messagingTemplate.convertAndSend("/queue/message/"+userId,
                objectMapper.writeValueAsString(messageDto));
    }

    public void refreshMessageNum(Long userId) {
        messagingTemplate.convertAndSend("/queue/message/unread/"+userId,
                messageService.getUnreadMessageNum(userId));
    }


}
