package com.liu.myblog.aspect;

import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.exception.BaseException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
public class RequiredAdminAspect {

    @Resource
    private HttpServletRequest request;

    //拦截加了RequiredAdmin注解的请求，判断是否是管理员
    @Around(value = "@annotation(com.liu.myblog.annotation.RequiredAdmin)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        //从session作用域中得到登录用户的userId
        Object admin = request.getAttribute("admin");
        //判断用户是否有权限
        if (admin == null) {
            throw new BaseException(CodeEnum.AUTH_ERROR.getCode(), "非管理员登录！");
        }
        //放行执行目标方法
        return pjp.proceed();
    }

}
