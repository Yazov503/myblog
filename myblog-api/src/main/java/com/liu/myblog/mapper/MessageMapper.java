package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    Long getUnreadMessageNum(@Param("receiverId") Long receiverId);

    List<Message> getMessages(@Param("userId") Long userId,
                              @Param("contactId") Long contactId,
                              @Param("start")int start,
                              @Param("limit") int limit);

    void changeAllUnread(@Param("receiverId")Long receiverId,@Param("senderId")Long senderId);
}
