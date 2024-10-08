package com.liu.myblog.rabbitmq;

import com.liu.myblog.config.RabbitMQConfig;
import com.liu.myblog.dao.Message;
import com.liu.myblog.dao.Notification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Service
public class MessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(Long senderId,Long receiverId, String content) {
        if (Objects.equals(senderId, receiverId)&&receiverId!=0) return;
        Message message = new Message();
        message.setSenderId(senderId).setReceiverId(receiverId)
                .setContent(content).setCreateTime(new Date());
        rabbitTemplate.convertAndSend(RabbitMQConfig.MESSAGE_EXCHANGE,
                RabbitMQConfig.MESSAGE_ROUTING_KEY, message);
    }

    public void sendMessage(Message message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.MESSAGE_EXCHANGE,
                RabbitMQConfig.MESSAGE_ROUTING_KEY, message);
    }

    public void sendNotification(Long senderId, Long receiverId,
                                 Long blogId,Long commentId,Long replyId, String content) {
        if (Objects.equals(senderId, receiverId)&&receiverId!=0) return;
        Notification notification = new Notification();
        notification.setSenderId(senderId).setReceiverId(receiverId)
                .setBlogId(blogId).setCommentId(commentId).setReplyId(replyId)
                .setContent(content).setCreateTime(new Date());
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY, notification);
    }

    public void sendNotification(Notification notification) {
        if (Objects.equals(notification.getSenderId(), notification.getReceiverId())
                &&notification.getReceiverId()!=0) return;
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY, notification);
    }
}
