package com.liu.myblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.liu.myblog.dao.Contact;
import com.liu.myblog.dao.Message;
import com.liu.myblog.dao.User;
import com.liu.myblog.dao.dto.MessageDto;
import com.liu.myblog.mapper.ContactMapper;
import com.liu.myblog.mapper.MessageMapper;
import com.liu.myblog.mapper.UserMapper;
import com.liu.myblog.service.MessageService;
import com.liu.myblog.util.sensitive.SensitiveWordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ContactMapper contactMapper;

    @Resource
    private SensitiveWordUtil sensitiveWordUtil;

    @Override
    public Long getUnreadMessageNum(Long userId) {
        return messageMapper.getUnreadMessageNum(userId);
    }

    @Override
    public void createMessage(Message message) {
        if (!contactMapper.existContact(message.getSenderId(), message.getReceiverId())) {
            contactMapper.insert(new Contact()
                    .setUserId(message.getSenderId())
                    .setContactId(message.getReceiverId()));
            contactMapper.insert(new Contact()
                    .setUserId(message.getReceiverId())
                    .setContactId(message.getSenderId()));
        }
        message.setContent(sensitiveWordUtil.replace(message.getContent()));
        messageMapper.insert(message);
        contactMapper.updateLastContactTime(message.getSenderId(), message.getReceiverId());
    }

    @Override
    public MessageDto convertToMessageDto(Message message) {
        MessageDto messageDto = new MessageDto();
        BeanUtil.copyProperties(message, messageDto);
        User user = userMapper.selectUserById(message.getSenderId());
        return messageDto.setSenderName(user.getUsername()).setSenderAvatar(user.getAvatar());
    }

    @Override
    public List<MessageDto> getMessages(Long userId, Long contactId, int start, int limit) {
        return messageMapper.getMessages(userId, contactId, start, limit)
                .stream().map(this::convertToMessageDto).collect(Collectors.toList());
    }

    @Override
    public void refreshUnreadMessage(Long userId, long contactId) {
        messageMapper.changeAllUnread(userId, contactId);
    }
}
