package com.liu.myblog.rabbitmq;

import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.config.RabbitMQConfig;
import com.liu.myblog.dao.Message;
import com.liu.myblog.dao.Notification;
import com.liu.myblog.dao.dto.MessageDto;
import com.liu.myblog.dao.dto.NotificationDto;
import com.liu.myblog.service.MessageService;
import com.liu.myblog.service.NotificationService;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.websocket.WebSocketHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageReceiver {

    @Resource
    private MessageService messageService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private WebSocketHandler webSocketHandler;

    @Resource
    private RedisUtil redisUtil;

    @RabbitListener(queues = RabbitMQConfig.MESSAGE_QUEUE)
    public void receiveMessage(Message message) {
        if(redisUtil.exist(RedisKeyConstant.OPEN_RABBITMQ)){
            if(redisUtil.get(RedisKeyConstant.OPEN_RABBITMQ).equals("0"))return;
        }else {
            redisUtil.set(RedisKeyConstant.OPEN_RABBITMQ, "1");
        }
        if (message != null) {
//            redisUtil.sAdd(RedisKeyConstant.MESSAGE + message.getReceiverId(), message);
            MessageDto messageDto = messageService.convertToMessageDto(message);
            if (messageDto == null||messageDto.getReceiverId()==null) return;
            messageService.createMessage(message);
            webSocketHandler.sendMessage(messageDto);
            webSocketHandler.refreshMessageNum(message.getReceiverId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveNotification(Notification notification) {
        if(redisUtil.exist(RedisKeyConstant.OPEN_RABBITMQ)){
            if(redisUtil.get(RedisKeyConstant.OPEN_RABBITMQ).equals("0"))return;
        }else {
            redisUtil.set(RedisKeyConstant.OPEN_RABBITMQ, "1");
        }
        if (notification != null) {
//            redisUtil.sAdd(RedisKeyConstant.NOTIFICATION + notification.getReceiverId(), notification);
            NotificationDto notificationDto = notificationService.convertToNotificationDto(notification);
            if (notificationDto == null) return;
            notificationService.createNotification(notification);
            webSocketHandler.sendNotification(notificationDto);
            webSocketHandler.refreshNotificationNum(notification.getReceiverId());
        }
    }
}
