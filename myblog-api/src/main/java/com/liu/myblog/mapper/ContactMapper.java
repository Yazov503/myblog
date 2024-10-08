package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Contact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContactMapper extends BaseMapper<Contact> {
    boolean existContact(@Param("senderId") Long senderId,@Param("receiverId") Long receiverId);

    void updateLastContactTime(@Param("senderId") Long senderId,
                               @Param("receiverId") Long receiverId);
}
