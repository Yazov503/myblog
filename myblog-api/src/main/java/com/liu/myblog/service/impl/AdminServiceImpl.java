package com.liu.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Admin;
import com.liu.myblog.dao.Notification;
import com.liu.myblog.mapper.AdminMapper;
import com.liu.myblog.mapper.NotificationMapper;
import com.liu.myblog.rabbitmq.MessageProducer;
import com.liu.myblog.service.AdminService;
import com.liu.myblog.util.TokenUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private NotificationMapper notificationMapper;

    @Override
    public ReturnData login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户不存在");
        Map<String, Object> data = new LinkedHashMap<>();
        String token = tokenUtil.generateToken(admin.getId(), true);
        data.put("token", token);
        return ReturnData.success(data);
    }

    @Override
    public ReturnData mute(long userId, int muteDay) {
        redisTemplate.opsForValue().set(RedisKeyConstant.MUTE + userId, true, muteDay, TimeUnit.DAYS);
        messageProducer.sendNotification(0L, userId, 0L, 0L, 0L,
                "你已被禁言" + muteDay + "天");
        return ReturnData.success();
    }

    @Override
    public ReturnData cancelMute(long userId) {
        Boolean hasKey = redisTemplate.hasKey(RedisKeyConstant.MUTE + userId);
        if (hasKey == null || !hasKey) {
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "该用户未被禁言");
        }
        redisTemplate.delete(RedisKeyConstant.MUTE + userId);
        messageProducer.sendNotification(0L, userId, 0L, 0L, 0L,
                "你已被解除禁言");
        return ReturnData.success();
    }

    @Override
    public ReturnData createOrUpdateBulletin(Notification bulletin) {
        if(bulletin==null)return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "未知公告");
        if(bulletin.getId()==null){
            messageProducer.sendNotification(0L, 0L,
                    0L,0L,0L, bulletin.getContent());
        }else{
            bulletin.setUpdateTime(new Date());
            notificationMapper.updateById(bulletin);
        }
        return ReturnData.success();
    }

    @Override
    public IPage<Notification> getBulletinByPage(int pageNum, int pageSize, String queryText) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id",0L).eq("receiver_id",0L).eq("is_deleted", false).orderByDesc("create_time");
        if(!queryText.isEmpty())
            queryWrapper.and(wrapper -> wrapper.like("content", queryText));
        return notificationMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Boolean deleteBulletin(long bulletinId) {
        notificationMapper.deleteNotificationById(bulletinId);
        return notificationMapper.getNotificationById(bulletinId) == null;
    }
}
