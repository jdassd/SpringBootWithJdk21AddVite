package com.example.demo.controller;

import com.example.demo.auth.AuthTokenStore;
import com.example.demo.auth.CaptchaService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final CaptchaService captchaService;
    private final UserRepository userRepository;
    private final AuthTokenStore tokenStore;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(CaptchaService captchaService, UserRepository userRepository, AuthTokenStore tokenStore) {
        this.captchaService = captchaService;
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    @GetMapping("/captcha")
    public CaptchaService.CaptchaResponse captcha() {
        return captchaService.generateCaptcha();
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Attempting registration for username: {}, email: {}", request.username(), request.email());
        validateCaptcha(request.captchaToken(), request.captchaCode());
        ensureUnique(request.username(), request.email());

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setFailedAttempts(0);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        log.info("Registration successful for user: {}", user.getUsername());
        String token = tokenStore.createToken(user.getId(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.username());

        // 1. Validate Captcha
        if (!captchaService.verify(request.captchaToken(), request.captchaCode())) {
            log.warn("Login failed: Invalid captcha for user {}", request.username());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码错误或已过期");
        }

        // 2. Find User
        User user = userRepository.findByUsername(request.username())
                .orElse(null);

        if (user == null) {
            log.warn("Login failed: User {} not found", request.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }

        // 3. Check Lock
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            log.warn("Login failed: Account {} is locked until {}", request.username(), user.getLockedUntil());
            throw new ResponseStatusException(HttpStatus.LOCKED, "账号因尝试次数过多已被临时锁定，请稍后再试");
        }

        // 4. Validate Password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
            attempts += 1;
            user.setFailedAttempts(attempts);

            String errorMessage = "密码错误";
            if (attempts >= 5) {
                user.setLockedUntil(Instant.now().plusSeconds(900));
                log.warn("Login failed: User {} reached max attempts and is now locked", request.username());
                errorMessage = "密码错误次数过多，账号已锁定 15 分钟";
            } else {
                log.warn("Login failed: Incorrect password for user {}. Attempt {}/5", request.username(), attempts);
                errorMessage = String.format("密码错误，还可重试 %d 次", 5 - attempts);
            }

            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
        }

        // 5. Success
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        log.info("Login successful for user: {}, role: {}", user.getUsername(), user.getRole());
        String token = tokenStore.createToken(user.getId(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        log.info("User logout requested");
        tokenStore.revoke(token);
    }

    private void validateCaptcha(String token, String code) {
        if (!captchaService.verify(token, code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码无效");
        }
    }

    private void ensureUnique(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Registration rejected: Username {} already exists", username);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该用户名已被注册");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration rejected: Email {} already exists", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该邮箱已被注册");
        }
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 32) String username,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank String captchaToken,
            @NotBlank String captchaCode) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String captchaToken,
            @NotBlank String captchaCode) {
    }

    public record AuthResponse(String token, String username, String role) {
    }
}
