package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.User;
import com.liu.myblog.dao.dto.ContactDto;
import com.liu.myblog.dao.dto.UserDto;
import com.liu.myblog.dao.vo.Follower;

import java.util.List;

public interface UserService {
    ReturnData login(String email, String password);

    ReturnData register(String email, String password,String code);

    User getUserInfo(Long userId);

    String getAvatar(Long userId);

    ReturnData updateUser(User user);

    ReturnData resetEmail(String email, String code, Long userId);

    ReturnData resetPassword(String oldPassword, String newPassword, Long userId);

    Boolean deleteUser(Long userId);

    ReturnData codeLogin(String email, String code);

    ReturnData sendCode(String email, int type);

    ReturnData resetPasswordWithCode(String email, String newPassword, String code);

    ReturnData followUser(long followId, Long userId);

    List<Follower> getAllFollowers(Long userId);

    Boolean checkFollow(long userId, Long userIdInToken);

    IPage<UserDto> getUsersByPage(int pageNum, int pageSize, String queryText);

    IPage<ContactDto> getContactsByPage(int pageNum, int pageSize, String queryText, Long userId);
}
