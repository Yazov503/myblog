package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    int getUnreadNotificationNum(Long userId);

    void changeAllUnread(Long userId);

    void deleteNotificationById(long notificationId);

    Notification getNotificationById(long bulletinId);
}
