package com.liu.myblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.common.VerificationCode;
import com.liu.myblog.config.AppConfig;
import com.liu.myblog.dao.Contact;
import com.liu.myblog.dao.User;
import com.liu.myblog.dao.dto.ContactDto;
import com.liu.myblog.dao.dto.UserDto;
import com.liu.myblog.dao.vo.Follower;
import com.liu.myblog.mapper.BlogMapper;
import com.liu.myblog.mapper.ContactMapper;
import com.liu.myblog.mapper.UserMapper;
import com.liu.myblog.service.UserService;
import com.liu.myblog.util.EmailUtil;
import com.liu.myblog.util.PasswordUtil;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private ContactMapper contactMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private EmailUtil emailUtil;

    @Override
    public ReturnData login(String email, String password) {
        User user = userMapper.selectByEmail(email);
        if (user == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户不存在");
        Map<String, Object> data = new LinkedHashMap<>();
        String token = tokenUtil.generateToken(user.getId(), false);
        data.put("token", token);
        return ReturnData.success(data);
    }

    @Override
    public ReturnData register(String email, String password, String code) {
        String key = getKey(email, VerificationCode.REGISTER.getCode());
        if (!redisUtil.exist(key))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码已过期");

        if (!redisUtil.get(key).equals(code))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码错误");

        if (userMapper.selectByEmail(email) != null)
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "该邮箱已被注册");

        redisUtil.remove(email);
        String username = appConfig.getDefaultUsername() + RandomUtil.randomString(10);
        while (userMapper.checkUsername(username)) {
            username = appConfig.getDefaultUsername() + RandomUtil.randomString(10);
        }
        User user = new User().setEmail(email).setUsername(username).setPassword(password);
        userMapper.insert(user);
        user = userMapper.selectByEmail(email);
        if (user == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "注册失败");

        redisUtil.remove(key);
        Map<String, Object> data = new LinkedHashMap<>();
        String token = tokenUtil.generateToken(user.getId(), false);
        data.put("token", token);
        return ReturnData.success(data);
    }

    @Override
    public User getUserInfo(Long userId) {
        return userMapper.selectUserById(userId);
    }

    @Override
    public String getAvatar(Long userId) {
        String avatar = userMapper.getAvatar(userId);
        avatar = avatar == null ? appConfig.getDefaultAvatar() : avatar;
        return avatar;
    }

    @Override
    public ReturnData updateUser(User user) {
        if (user.getUsername() != null && userMapper.checkUsernameWithId(user.getId(), user.getUsername()))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户名重复");
        if (user.getEmail() != null && userMapper.checkEmailWithId(user.getId(), user.getEmail()))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "邮箱已注册");
        userMapper.updateById(user.setUpdateTime(new Date()));
        User user1 = userMapper.selectUserById(user.getId()).setPassword(null);
        return ReturnData.success(user1);
    }

    @Override
    public ReturnData resetEmail(String email, String code, Long userId) {
        String key = getKey(email, VerificationCode.RESET_EMAIL.getCode());
        if (!redisUtil.exist(key))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码已过期");

        if (!redisUtil.get(key).equals(code))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码错误");

        if (userMapper.selectByEmail(email) != null)
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "该邮箱已被注册");

        userMapper.updateById(userMapper.selectUserById(userId).setEmail(email));
        redisUtil.remove(key);
        return ReturnData.success();
    }

    @Override
    public ReturnData resetPassword(String oldPassword, String newPassword, Long userId) {
        String password = userMapper.selectUserById(userId).getPassword();
        if (password.equals(oldPassword)) {
            userMapper.updateById(userMapper.selectUserById(userId).setPassword(newPassword));
            return ReturnData.success();
        }
        return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "原密码错误");
    }

    @Override
    @Transactional
    public Boolean deleteUser(Long userId) {
        blogMapper.deleteTagsByUserId(userId);
        blogMapper.deleteImagesByUserId(userId);
        blogMapper.deleteBlogsByUserId(userId);
        userMapper.deleteUserById(userId);
        return userMapper.selectUserById(userId) == null;
    }

    @Override
    public ReturnData codeLogin(String email, String code) {
        String key = getKey(email, VerificationCode.LOGIN.getCode());
        if (!redisUtil.exist(key))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码已过期");

        if (!redisUtil.get(key).equals(code))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码错误");

        if (userMapper.selectByEmail(email) == null) {
            String username = appConfig.getDefaultUsername() + RandomUtil.randomString(10);
            while (userMapper.checkUsername(username)) {
                username = appConfig.getDefaultUsername() + RandomUtil.randomString(10);
            }
            userMapper.insert(
                    new User().setEmail(email)
                            .setUsername(username)
                            .setPassword(PasswordUtil.generatePassword(12)));
        }

        User user = userMapper.selectByEmail(email);
        if (user != null) {
            LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
            String token = tokenUtil.generateToken(user.getId(), false);
            redisUtil.remove(key);
            data.put("token", token);
            return ReturnData.success(data);
        }
        return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "登录失败");
    }

    @Override
    public ReturnData sendCode(String email, int type) {
        String code = RandomUtil.randomNumbers(6);
        try {
            String key = getKey(email, type);
            emailUtil.sendEmail(email, code, type);
            redisUtil.remove(key);
            redisUtil.set(key, code, 300L);
        } catch (MessagingException e) {
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码发送失败");
        }
        return ReturnData.success();
    }

    @Override
    public ReturnData resetPasswordWithCode(String email, String newPassword, String code) {
        String key = getKey(email, VerificationCode.RESET_PASSWORD.getCode());
        System.out.println(key);
        if (!redisUtil.exist(key))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码已过期");

        if (!redisUtil.get(key).equals(code))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "验证码错误");

        userMapper.updateById(userMapper.selectByEmail(email).setPassword(newPassword));
        redisUtil.remove(key);
        return ReturnData.success();
    }

    @Override
    public ReturnData followUser(long followId, Long userId) {
        String key = RedisKeyConstant.FOLLOW_USER + userId;
        Boolean followed = userMapper.checkFollowed(followId, userId);
        if (followed) {
            Boolean remove = userMapper.removeFollow(followId, userId);
            if (remove) {
                redisUtil.sRemove(key, followId);
            } else {
                return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "取消关注失败");
            }
        } else {
            Boolean save = userMapper.addFollow(followId, userId);
            if (save) {
                redisUtil.sAdd(key, followId);
            } else {
                return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "关注失败");
            }
        }
        return ReturnData.success();
    }

    @Override
    public List<Follower> getAllFollowers(Long userId) {
        return userMapper.selectAllFollowers(userId);
    }

    @Override
    public Boolean checkFollow(long followId, Long userId) {
        return userMapper.checkFollowed(followId, userId);
    }

    @Override
    public IPage<UserDto> getUsersByPage(int pageNum, int pageSize, String queryText) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false).orderByDesc("create_time");
        if (StringUtils.isNotEmpty(queryText))
            queryWrapper.and(wrapper -> wrapper.like("username", queryText)
                    .or().like("email", queryText));

        return userMapper.selectPage(page, queryWrapper).convert(user -> {
            UserDto userDto = new UserDto();
            BeanUtil.copyProperties(user, userDto);
            userDto.setIsMuted(redisUtil.exist(RedisKeyConstant.MUTE + userDto.getId()));
            return userDto;
        });
    }

    @Override
    public IPage<ContactDto> getContactsByPage(int pageNum, int pageSize, String queryText, Long userId) {
        Page<Contact> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Contact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_deleted", false)
                .orderByDesc("last_contact_time");
        if (StringUtils.isNotEmpty(queryText))
            queryWrapper.and(wrapper -> wrapper.like("username", queryText));

        return contactMapper.selectPage(page, queryWrapper).convert(contact -> {
            ContactDto contactDto = new ContactDto();
            BeanUtil.copyProperties(contact, contactDto);
            User user = userMapper.selectUserById(contact.getContactId());
            BeanUtil.copyProperties(user, contactDto);
            return contactDto;
        });
    }

    private String getKey(String email, int type) {
        switch (type) {
            case 1:
                return RedisKeyConstant.VERIFICATION_CODE_REGISTER + email;
            case 2:
                return RedisKeyConstant.VERIFICATION_CODE_RESET_EMAIL + email;
            case 3:
                return RedisKeyConstant.VERIFICATION_CODE_LOGIN + email;
            case 4:
                return RedisKeyConstant.VERIFICATION_CODE_RESET_PASSWORD + email;
            default:
                return null;
        }
    }
}
