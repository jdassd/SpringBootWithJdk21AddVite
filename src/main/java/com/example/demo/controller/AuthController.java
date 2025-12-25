package com.example.demo.controller;

import com.example.demo.auth.AuthTokenStore;
import com.example.demo.auth.CaptchaService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
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
        String token = tokenStore.createToken(user.getId(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        validateCaptcha(request.captchaToken(), request.captchaCode());
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Account locked");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
            attempts += 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(Instant.now().plusSeconds(900));
            }
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        String token = tokenStore.createToken(user.getId(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        tokenStore.revoke(token);
    }

    private void validateCaptcha(String token, String code) {
        if (!captchaService.verify(token, code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid captcha");
        }
    }

    private void ensureUnique(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email exists");
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
