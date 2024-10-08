package com.liu.myblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.dto.NotificationDto;
import com.liu.myblog.service.NotificationService;
import com.liu.myblog.util.TokenUtil;
import com.liu.myblog.websocket.WebSocketHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private NotificationService notificationService;

    @Resource
    private WebSocketHandler webSocketHandler;

    @GetMapping("/unread")
    public ReturnData getUnreadNotificationNum(HttpServletRequest request) {
        Long userId = tokenUtil.getUserId(request);
        return ReturnData.success(notificationService.getUnreadNotificationNum(userId));
    }

    @GetMapping("/page")
    public ReturnData getNotificationsByPage(@RequestParam("pageNum") int pageNum,
                                             @RequestParam("pageSize") int pageSize,
                                             HttpServletRequest request) {
        Long userId = tokenUtil.getUserId(request);
        IPage<NotificationDto> notificationDtoIPage =
                notificationService.getNotificationsByPage(pageNum, pageSize, userId);
        webSocketHandler.refreshNotificationNum(userId);
        return ReturnData.success(notificationDtoIPage);
    }
}
