package com.example.demo.interceptor;

import com.example.demo.auth.AuthTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public class AdminInterceptor implements HandlerInterceptor {
    private final AuthTokenStore tokenStore;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<String> publicPatterns = List.of(
        "/api/auth/**",
        "/api/rss",
        "/api/blog/posts/public",
        "/api/blog/posts/*",
        "/api/blog/posts/*/comments",
        "/api/navigation/public",
        "/api/search-engines/public",
        "/api/pages/public/**",
        "/api/gallery/public/**"
    );

    public AdminInterceptor(AuthTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (isPublic(path)) {
            return true;
        }
        String token = request.getHeader("X-Auth-Token");
        String role = tokenStore.getRole(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return true;
    }

    private boolean isPublic(String path) {
        return publicPatterns.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }
}
