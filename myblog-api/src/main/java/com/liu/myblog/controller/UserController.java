package com.liu.myblog.controller;


import cn.hutool.core.bean.BeanUtil;
import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.annotation.SkipLoginCheck;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.User;
import com.liu.myblog.dao.dto.UserDto;
import com.liu.myblog.service.UserService;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private RedisUtil redisUtil;

    @PostMapping("/login")
    @SkipLoginCheck
    public ReturnData login(String email, String password) {
        return userService.login(email, password);
    }

    @PostMapping("/code-login")
    @SkipLoginCheck
    public ReturnData codeLogin(String email, String code) {
        return userService.codeLogin(email, code);
    }

    @PostMapping("/register")
    @SkipLoginCheck
    public ReturnData register(String email, String password, String code) {
        return userService.register(email, password, code);
    }

    @PostMapping("/code")
    @SkipLoginCheck
    public ReturnData getCode(String email, Integer type) {
        return userService.sendCode(email, type);
    }

    @GetMapping("/{id}")
    @SkipLoginCheck
    public ReturnData getUserInfo(@PathVariable("id") long userId, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getUserInfo(userId);
        if (user == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户不存在");

        UserDto userDto = new UserDto();
        BeanUtil.copyProperties(user, userDto);
        String token = request.getHeader("token");
        if (token == null || tokenUtil.isTokenExpired(token)) {
            userDto.setEmail(null);
        } else if (tokenUtil.getIdFromToken(token) != userId) {
            userDto.setEmail(null);
            Long userIdInToken = tokenUtil.getUserId(request);
            Boolean isFollowed = redisUtil.sIsMember(RedisKeyConstant.FOLLOW_USER + userIdInToken, userId);
            if (isFollowed == null) {
                isFollowed = userService.checkFollow(userId, userIdInToken);
                redisUtil.sAdd(RedisKeyConstant.FOLLOW_USER + userIdInToken, userId);
            }
            userDto.setIsFollowed(isFollowed);
        } else {
            tokenUtil.getUserId(request);
        }
        userDto.setPassword(null);
        return ReturnData.success(userDto);
    }

    @GetMapping("/getHeadImg/{id}")
    @SkipLoginCheck
    public ReturnData getHeadImg(@PathVariable("id") long userId) {
        return ReturnData.success(userService.getAvatar(userId));
    }

    @GetMapping("/getUserId")
    public ReturnData getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        return ReturnData.success(tokenUtil.getIdFromToken(token));
    }

    @GetMapping("/check")
    public ReturnData checkToken(HttpServletRequest request) {
        return ReturnData.success(!tokenUtil.isTokenExpired(request.getHeader("token")));
    }

    @PutMapping
    public ReturnData updateUserInfo(@RequestBody User user,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        tokenUtil.getUserId(request);
        return userService.updateUser(user);
    }

    @PutMapping("/reset-email")
    public ReturnData resetEmail(String email, String code,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);
        return userService.resetEmail(email, code, userId);
    }

    @PutMapping("/reset-password")
    public ReturnData resetPassword(String oldPassword, String newPassword, String code,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Long userId = tokenUtil.getIdFromToken(token);
        return userService.resetPassword(oldPassword, newPassword, userId);
    }

    @PutMapping("/reset-password-code")
    public ReturnData resetPasswordWithCode(String email, String newPassword, String code,
                                            HttpServletRequest request) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        return userService.resetPasswordWithCode(email, newPassword, code);
    }

    @DeleteMapping
    public ReturnData deleteUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Long userId = tokenUtil.getIdFromToken(token);
        Boolean deleted = userService.deleteUser(userId);
        if (!deleted) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "删除失败");
        return ReturnData.success();
    }

    @DeleteMapping("/{id}")
    @RequiredAdmin
    public ReturnData deleteUserById(@PathVariable("id") Long userId, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Boolean deleted = userService.deleteUser(userId);
        if (!deleted) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "删除失败");
        return ReturnData.success();
    }

    @PostMapping("/follow")
    public ReturnData followUser(long followId, HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);
        return userService.followUser(followId, userId);
    }

    @GetMapping("/getFollows")
    public ReturnData getAllFollowers(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);
        return ReturnData.success(userService.getAllFollowers(userId));
    }

    @GetMapping("/page")
    public ReturnData getUsersByPage(@RequestParam("pageNum") int pageNum,
                                     @RequestParam("pageSize") int pageSize,
                                     @RequestParam(value = "queryText", required = false, defaultValue = "")
                                     String queryText) {
        return ReturnData.success(userService.getUsersByPage(pageNum, pageSize, queryText));
    }

    @GetMapping("/contact/page")
    public ReturnData getContactsByPage(@RequestParam("pageNum") int pageNum,
                                        @RequestParam("pageSize") int pageSize,
                                        @RequestParam(value = "queryText", required = false, defaultValue = "")
                                        String queryText,
                                        HttpServletRequest request) {
        Long userId = tokenUtil.getUserId(request);
        return ReturnData.success(userService.getContactsByPage(pageNum, pageSize, queryText, userId));
    }
}
