package com.springsns.config;

import com.springsns.interceptor.BearerAuthInterceptor;
import com.springsns.interceptor.LogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final BearerAuthInterceptor bearerAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .order(1)
                .addPathPatterns("/**");

        registry.addInterceptor(bearerAuthInterceptor)
                .order(2)
                .addPathPatterns("/**")
                .excludePathPatterns("/account/sign-in","/account/check-email-token","/post/image/**");

    }
}
