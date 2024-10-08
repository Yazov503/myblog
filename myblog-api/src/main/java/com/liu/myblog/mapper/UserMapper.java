package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.User;
import com.liu.myblog.dao.vo.Follower;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectByEmail(String email);

    User selectUserById(long userId);

    String selectUsernameByUserId(long userId);

    String getAvatar(Long userId);

    void deleteUserById(Long userId);

    String selectRepliedUsernameByReplyId(long replyId);

    Boolean checkFollowed(@Param("followId") long followId, @Param("userId") Long userId);

    Boolean removeFollow(@Param("followId") long followId, @Param("userId") Long userId);

    Boolean addFollow(@Param("followId") long followId, @Param("userId") Long userId);

    boolean checkUsername(String username);

    boolean checkUsernameWithId(@Param("userId") long userId, @Param("username") String username);

    List<Follower> selectAllFollowers(long userId);

    boolean checkEmailWithId(@Param("userId") Long id, @Param("email") String email);

}
