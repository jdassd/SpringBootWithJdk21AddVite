package com.example.demo.service;

import com.example.demo.entity.Site;
import com.example.demo.entity.User;
import com.example.demo.repository.SiteRepository;
import com.example.demo.security.ContentSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class SiteService {
    private final SiteRepository siteRepository;
    private final ContentSanitizer sanitizer;

    public SiteService(SiteRepository siteRepository, ContentSanitizer sanitizer) {
        this.siteRepository = siteRepository;
        this.sanitizer = sanitizer;
    }

    public Site resolveSite(String siteKey) {
        return siteRepository.findBySiteKey(siteKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found"));
    }

    public Site ensureSiteForUser(User user) {
        return siteRepository.findByOwnerUserId(user.getId())
                .orElseGet(() -> createDefaultSite(user));
    }

    public Site updateSite(Long userId, Site updates) {
        Site site = siteRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found"));
        if (updates.getDisplayName() != null && !updates.getDisplayName().isBlank()) {
            site.setDisplayName(updates.getDisplayName().trim());
        }
        if (updates.getDescription() != null) {
            site.setDescription(updates.getDescription().trim());
        }
        if (updates.getAvatarUrl() != null) {
            site.setAvatarUrl(updates.getAvatarUrl().trim());
        }
        if (updates.getBio() != null) {
            site.setBio(updates.getBio().trim());
        }
        if (updates.getSocialLinks() != null) {
            site.setSocialLinks(updates.getSocialLinks().trim());
        }
        if (updates.getTheme() != null) {
            site.setTheme(updates.getTheme().trim());
        }
        if (updates.getThemePrimary() != null) {
            site.setThemePrimary(updates.getThemePrimary().trim());
        }
        if (updates.getThemeAccent() != null) {
            site.setThemeAccent(updates.getThemeAccent().trim());
        }
        if (updates.getCustomCss() != null) {
            site.setCustomCss(sanitizer.sanitizeCss(updates.getCustomCss()));
        }
        site.setUpdatedAt(Instant.now());
        return siteRepository.save(site);
    }

    public String ensureUniqueSiteKey(String baseKey) {
        String sanitized = slugify(baseKey);
        String candidate = sanitized.isBlank() ? "site" : sanitized;
        while (siteRepository.existsBySiteKey(candidate)) {
            candidate = candidate + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
        return candidate;
    }

    private Site createDefaultSite(User user) {
        Site site = new Site();
        site.setOwnerUserId(user.getId());
        site.setDisplayName(user.getUsername());
        site.setSiteKey(ensureUniqueSiteKey(user.getUsername()));
        site.setAvatarUrl(user.getAvatarUrl());
        site.setBio(user.getBio());
        site.setTheme("classic");
        site.setCreatedAt(Instant.now());
        site.setUpdatedAt(Instant.now());
        return siteRepository.save(site);
    }

    private String slugify(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
