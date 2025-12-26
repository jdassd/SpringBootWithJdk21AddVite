package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {
    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public PagedResponse<UserSummary> listUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Page<User> users = userRepository.findAll(PaginationUtils.toPageable(page, size, sort, "createdAt"));
        List<UserSummary> items = users.getContent().stream()
                .map(user -> new UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                        user.getCreatedAt()))
                .toList();
        return new PagedResponse<>(items, users.getNumber(), users.getSize(), users.getTotalElements());
    }

    @PatchMapping("/{id}/role")
    public void updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
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

    public record RoleUpdateRequest(@NotBlank String role) {
    }
}
