package com.liu.myblog.interceptor;

import cn.hutool.json.JSONUtil;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 博客模块的拦截器
 * 作用：判断用户是否禁言
 */
@Component
public class MuteInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private TokenUtil tokenUtil;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String token = request.getHeader("token");
        if (token != null && tokenUtil.validateToken(token)) {
            Long userId = tokenUtil.getIdFromToken(token);
            boolean mute = redisUtil.exist(RedisKeyConstant.MUTE + userId);
            if (mute) {
                Long expire = redisUtil.getExpire(RedisKeyConstant.MUTE + userId);
//                throw new BaseException(CodeEnum.MUTE_ERROR.getCode(),
//                "你已被禁言,结束时间为：" + simpleDateFormat.format(new Date(System.currentTimeMillis() + expire * 1000)));
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(JSONUtil.toJsonStr(ReturnData.fail(CodeEnum.MUTE_ERROR.getCode(),
                        "你已被禁言,结束时间为：" + simpleDateFormat.format(
                                new Date(System.currentTimeMillis() + expire * 1000)))));
                return false;
            }
        }
        return true;
    }
}
