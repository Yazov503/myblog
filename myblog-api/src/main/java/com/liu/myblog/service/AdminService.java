package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Notification;

public interface AdminService {

    ReturnData login(String username, String password);

    ReturnData mute(long userId, int muteDay);

    ReturnData cancelMute(long userId);

    ReturnData createOrUpdateBulletin(Notification bulletin);

    IPage<Notification> getBulletinByPage(int pageNum, int pageSize, String queryText);

    Boolean deleteBulletin(long bulletinId);
}
