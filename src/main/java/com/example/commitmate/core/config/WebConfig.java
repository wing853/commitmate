package com.example.commitmate.core.config;

import com.example.commitmate.core.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/groups/**", "/todo/**","/logout")
                .excludePathPatterns("/login", "/login-form", "/join", "/join-form", "/css/**", "/js/**", "/images/**");
    }
}
