package com.liu.myblog.config;

import com.liu.myblog.filter.JwtAuthenticationFilter;
import com.liu.myblog.util.TokenUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private RequestMappingHandlerMapping handlerMapping;

    @Resource
    private CorsConfig corsConfig;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .addFilterBefore(new JwtAuthenticationFilter(tokenUtil, handlerMapping), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
//                .antMatchers("/auth/refresh").permitAll()
//                .antMatchers("/blog/**", "/user/**").authenticated()
                .anyRequest().permitAll()
                .and();
    }
}
