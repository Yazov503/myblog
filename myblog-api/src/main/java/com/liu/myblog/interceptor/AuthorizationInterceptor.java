package com.liu.myblog.interceptor;

import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.annotation.SkipLoginCheck;
import com.liu.myblog.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Resource
    private TokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果请求处理器不是方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        // 如果方法上有 @SkipLoginCheck 注解，则跳过登录检查
        if (method.isAnnotationPresent(SkipLoginCheck.class)) {
            return true;
        }

        // 默认情况下，进行登录检查
        String token = request.getHeader("token");
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in. Token is missing in the request header.");
            return false;
        }

        if (tokenUtil.isTokenExpired(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token已过期");
            return false;
        }

        if (method.isAnnotationPresent(RequiredAdmin.class)) {
            Boolean isAdmin = tokenUtil.getIsAdminFromToken(token);
            if(isAdmin){

            }
            return false;
        }

        return true; // 通过验证，继续处理请求
    }
}
