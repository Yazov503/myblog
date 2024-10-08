package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.dao.Notification;
import com.liu.myblog.dao.dto.NotificationDto;

public interface NotificationService {
    int getUnreadNotificationNum(Long userId);

    IPage<NotificationDto> getNotificationsByPage(int pageNum, int pageSize, Long userId);

    NotificationDto convertToNotificationDto(Notification notification);

    void createNotification(Notification notification);
}
