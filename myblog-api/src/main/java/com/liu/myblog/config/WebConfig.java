package com.liu.myblog.config;

import com.liu.myblog.interceptor.ApiIdempotentInterceptor;
import com.liu.myblog.interceptor.AuthorizationInterceptor;
import com.liu.myblog.interceptor.MuteInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AuthorizationInterceptor authorizationInterceptor;

    @Resource
    private MuteInterceptor muteInterceptor;

    @Resource
    private ApiIdempotentInterceptor apiIdempotentInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authorizationInterceptor)
//                .addPathPatterns("/blog/**")
//                .addPathPatterns("/user/**");
//    }

        @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(muteInterceptor)
                .addPathPatterns("/blog/comment")
                .addPathPatterns("/blog/comment/reply");
        registry.addInterceptor(apiIdempotentInterceptor)
                .addPathPatterns("/blog/**");
    }

}
