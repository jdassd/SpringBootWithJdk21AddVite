package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserSummary> listUsers() {
        return userRepository.findAll().stream()
            .map(user -> new UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt()))
            .toList();
    }

    public record UserSummary(Long id, String username, String email, java.time.Instant createdAt) {
    }
}
