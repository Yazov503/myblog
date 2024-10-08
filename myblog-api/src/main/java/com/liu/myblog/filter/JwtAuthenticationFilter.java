package com.liu.myblog.filter;

import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.annotation.SkipLoginCheck;
import com.liu.myblog.dao.JwtAuthentication;
import com.liu.myblog.util.TokenUtil;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtil tokenUtil;

    private RequestMappingHandlerMapping handlerMapping;

    public JwtAuthenticationFilter(TokenUtil tokenUtil, RequestMappingHandlerMapping handlerMapping) {
        this.tokenUtil = tokenUtil;
        this.handlerMapping = handlerMapping;
    }

    private static final String FILTER_APPLIED = "__spring_security_demoFilter_filterApplied";

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        if (request.getAttribute(FILTER_APPLIED) != null) {
            return;
        }
        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
        HandlerMethod handlerMethod = null;
        if (handlerExecutionChain != null) {
            handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();
        }
        Method method = null;
        if (handlerMethod != null) {
            method = handlerMethod.getMethod();
        }
        if (method != null && method.isAnnotationPresent(SkipLoginCheck.class)) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("token");
        if (token != null && tokenUtil.validateToken(token)) {
            Long userId = tokenUtil.getIdFromToken(token);
            Boolean isAdmin = tokenUtil.getIsAdminFromToken(token);
            Authentication authentication = new JwtAuthentication(userId, isAdmin);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (method != null && method.isAnnotationPresent(RequiredAdmin.class) && !isAdmin) {
                return;
            }

            if (tokenUtil.isTokenExpiringSoon(token)) {
                response.addHeader("Access-Control-Expose-Headers", "token");
                response.addHeader("token", tokenUtil.generateToken(userId, isAdmin));
            }
        }

        chain.doFilter(request, response);
    }

}
