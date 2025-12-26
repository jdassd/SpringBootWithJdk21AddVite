package com.example.demo.interceptor;

import com.example.demo.auth.AuthTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminInterceptor implements HandlerInterceptor {
    private final AuthTokenStore tokenStore;

    public AdminInterceptor(AuthTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String role = (String) request.getAttribute("userRole");
        if (role == null) {
            String token = request.getHeader("X-Auth-Token");
            role = tokenStore.getRole(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token"));
        }
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return true;
    }
}
