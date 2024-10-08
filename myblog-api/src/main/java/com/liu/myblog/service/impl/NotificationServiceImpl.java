package com.liu.myblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.myblog.dao.Notification;
import com.liu.myblog.dao.User;
import com.liu.myblog.dao.dto.NotificationDto;
import com.liu.myblog.mapper.BlogMapper;
import com.liu.myblog.mapper.NotificationMapper;
import com.liu.myblog.mapper.UserMapper;
import com.liu.myblog.service.NotificationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private BlogMapper blogMapper;


    @Override
    public int getUnreadNotificationNum(Long userId) {
        return notificationMapper.getUnreadNotificationNum(userId);
    }

    @Override
    public IPage<NotificationDto> getNotificationsByPage(int pageNum, int pageSize, Long userId) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false)
                .and(wrapper -> wrapper.eq("receiver_id", userId)
                        .or(w -> w.eq("receiver_id", 0).eq("sender_id", 0)))
                .orderByDesc("create_time");

        notificationMapper.changeAllUnread(userId);

        return notificationMapper.selectPage(page, queryWrapper).convert(this::convertToNotificationDto);
    }

    @Override
    public NotificationDto convertToNotificationDto(Notification notification) {
        NotificationDto notificationDto = new NotificationDto();
        BeanUtil.copyProperties(notification, notificationDto);
        if (notification.getSenderId() != 0) {
            User sender = userMapper.selectUserById(notification.getSenderId());
            notificationDto.setSenderName(sender.getUsername())
                    .setSenderAvatar(sender.getAvatar());
        }
        if (notificationDto.getReceiverId() == null) {
            return null;
        }
        return notificationDto.setReceiverName(userMapper.selectUsernameByUserId(notification.getReceiverId()))
                .setBlogTitle(blogMapper.selectTitleById(notification.getBlogId()));
    }

    @Override
    public void createNotification(Notification notification) {
        notificationMapper.insert(notification);;
    }
}
