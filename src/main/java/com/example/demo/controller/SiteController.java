package com.example.demo.controller;

import com.example.demo.entity.Site;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SiteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/site")
@CrossOrigin(origins = "*")
public class SiteController {
    private final SiteService siteService;
    private final UserRepository userRepository;

    public SiteController(SiteService siteService, UserRepository userRepository) {
        this.siteService = siteService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public SiteResponse getMySite(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Site site = siteService.ensureSiteForUser(user);
        return SiteResponse.from(site);
    }

    @PutMapping("/me")
    public SiteResponse updateMySite(HttpServletRequest request, @Valid @RequestBody SiteUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        Site updates = new Site();
        updates.setDisplayName(updateRequest.displayName());
        updates.setDescription(updateRequest.description());
        updates.setAvatarUrl(updateRequest.avatarUrl());
        updates.setBio(updateRequest.bio());
        updates.setSocialLinks(updateRequest.socialLinks());
        updates.setTheme(updateRequest.theme());
        updates.setThemePrimary(updateRequest.themePrimary());
        updates.setThemeAccent(updateRequest.themeAccent());
        updates.setCustomCss(updateRequest.customCss());
        Site site = siteService.updateSite(userId, updates);
        return SiteResponse.from(site);
    }

    public record SiteUpdateRequest(
            @NotBlank String displayName,
            String description,
            String avatarUrl,
            String bio,
            String socialLinks,
            String theme,
            String themePrimary,
            String themeAccent,
            String customCss) {
    }

    public record SiteResponse(
            String siteKey,
            String displayName,
            String description,
            String avatarUrl,
            String bio,
            String socialLinks,
            String theme,
            String themePrimary,
            String themeAccent,
            String customCss) {
        public static SiteResponse from(Site site) {
            return new SiteResponse(site.getSiteKey(), site.getDisplayName(), site.getDescription(), site.getAvatarUrl(),
                    site.getBio(), site.getSocialLinks(), site.getTheme(), site.getThemePrimary(), site.getThemeAccent(),
                    site.getCustomCss());
        }
    }

}
