package com.example.demo.config;

import com.example.demo.auth.AuthTokenStore;
import com.example.demo.auth.CaptchaService;
import com.example.demo.interceptor.AdminInterceptor;
import com.example.demo.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthConfig implements WebMvcConfigurer {
    @Bean
    public CaptchaService captchaService(@Value("${app.captcha.debug:false}") boolean debug) {
        return new CaptchaService(debug);
    }

    @Bean
    public AuthTokenStore authTokenStore() {
        return new AuthTokenStore();
    }

    @Bean
    public AuthInterceptor authInterceptor(AuthTokenStore tokenStore) {
        return new AuthInterceptor(tokenStore);
    }

    @Bean
    public AdminInterceptor adminInterceptor(AuthTokenStore tokenStore) {
        return new AdminInterceptor(tokenStore);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor(authTokenStore()))
            .addPathPatterns("/api/**");
        registry.addInterceptor(adminInterceptor(authTokenStore()))
            .addPathPatterns("/api/admin/**");
    }
}
