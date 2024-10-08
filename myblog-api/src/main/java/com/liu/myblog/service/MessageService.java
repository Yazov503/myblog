package com.liu.myblog.service;

import com.liu.myblog.dao.Message;
import com.liu.myblog.dao.dto.MessageDto;

import java.util.List;

public interface MessageService {
    Long getUnreadMessageNum(Long userId);

    void createMessage(Message message);

    MessageDto convertToMessageDto(Message message);

    List<MessageDto> getMessages(Long userId, Long contactId, int start, int limit);

    void refreshUnreadMessage(Long userId, long contactId);
}
