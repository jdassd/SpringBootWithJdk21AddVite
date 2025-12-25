package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserSummary> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                        user.getCreatedAt()))
                .toList();
    }

    @GetMapping("/me")
    public UserProfile getMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserProfile(user.getUsername(), user.getEmail(), user.getRole(), user.getAvatarUrl(), user.getBio());
    }

    @PutMapping("/me")
    public void updateMe(HttpServletRequest request, @RequestBody ProfileUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId).orElseThrow();
        user.setEmail(updateRequest.email());
        user.setAvatarUrl(updateRequest.avatarUrl());
        user.setBio(updateRequest.bio());
        userRepository.save(user);
    }

    @PostMapping("/me/password")
    public void changePassword(HttpServletRequest request, @RequestBody PasswordChangeRequest pwRequest) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId).orElseThrow();

        if (!passwordEncoder.matches(pwRequest.oldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Old password incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(pwRequest.newPassword()));
        userRepository.save(user);
    }

    @PatchMapping("/{id}/role")
    public void updateRole(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setRole(request.role());
        userRepository.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    public record UserSummary(Long id, String username, String email, String role, java.time.Instant createdAt) {
    }

    public record UserProfile(String username, String email, String role, String avatarUrl, String bio) {
    }

    public record ProfileUpdateRequest(String email, String avatarUrl, String bio) {
    }

    public record PasswordChangeRequest(String oldPassword, String newPassword) {
    }

    public record RoleUpdateRequest(String role) {
    }
}
