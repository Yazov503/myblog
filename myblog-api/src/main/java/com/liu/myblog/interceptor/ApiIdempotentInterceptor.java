package com.liu.myblog.interceptor;

import cn.hutool.json.JSONUtil;
import com.liu.myblog.annotation.ApiIdempotent;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

@Component
public class ApiIdempotentInterceptor implements HandlerInterceptor {


    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private RedisUtil redisUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        if (method.isAnnotationPresent(ApiIdempotent.class)) {
            String token = request.getHeader("token");
            if (token != null && tokenUtil.validateToken(token)) {
                if (redisUtil.exist(RedisKeyConstant.TOKEN + token)) {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.getWriter().write(JSONUtil.toJsonStr(ReturnData.fail(CodeEnum.TOO_MANY_REQUESTS.getCode(),
                            "请勿重复提交")));
                    return false;
                }
                redisUtil.set(RedisKeyConstant.TOKEN + token, "1", 1L);
            }
        }
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
        if (redisUtil.exist(RedisKeyConstant.TOKEN + request.getHeader("token"))) {
            redisUtil.remove(RedisKeyConstant.TOKEN + request.getHeader("token"));
        }
    }
}