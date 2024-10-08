package com.liu.myblog.aspect;

import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.exception.BaseException;
import com.liu.myblog.util.TokenUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RequiredLoginAspect {

    @Resource
    private HttpServletRequest request;

    @Resource
    private TokenUtil tokenUtil;

    @Before("@annotation(com.liu.myblog.annotation.SkipLoginCheck)")
    public void checkLogin() {
        String token = request.getHeader("token");
        if (token == null) {
            throw new BaseException(CodeEnum.AUTH_ERROR,"接收到一个没有token的请求");
        }
        
        String userId = String.valueOf(tokenUtil.getIdFromToken(token));
        if (userId == null) {
            throw new BaseException(CodeEnum.AUTH_ERROR,"接收到一个没有token中没有携带userId的请求");
        }
    }
}
