package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.CustomPage;
import com.example.demo.repository.CustomPageRepository;
import com.example.demo.security.ContentSanitizer;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/pages")
@CrossOrigin(origins = "*")
public class CustomPageController {
    private final CustomPageRepository pageRepository;
    private final ContentSanitizer sanitizer;

    public CustomPageController(CustomPageRepository pageRepository, ContentSanitizer sanitizer) {
        this.pageRepository = pageRepository;
        this.sanitizer = sanitizer;
    }

    @GetMapping
    public PagedResponse<CustomPage> listPages(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<CustomPage> pages = pageRepository.findByUserId(userId, PaginationUtils.toPageable(page, size, sort, "updatedAt"));
        return new PagedResponse<>(pages.getContent(), pages.getNumber(), pages.getSize(), pages.getTotalElements());
    }

    @PostMapping
    public CustomPage create(HttpServletRequest request, @Valid @RequestBody PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        CustomPage page = new CustomPage();
        page.setUserId(userId);
        page.setTitle(pageRequest.title());
        page.setSlug(ensureUniqueSlug(userId, slugify(pageRequest.slug()), null));
        page.setContent(sanitizer.sanitizeRichText(pageRequest.content()));
        page.setCustomCss(sanitizer.sanitizeCss(pageRequest.customCss()));
        page.setVisibility(resolveVisibility(pageRequest.visibility()));
        page.setPublishedAt(resolvePublishedAt(pageRequest.publishedAt(), page.getVisibility(), null));
        page.setCreatedAt(Instant.now());
        page.setUpdatedAt(Instant.now());
        pageRepository.save(page);
        return page;
    }

    @PutMapping("/{id}")
    public CustomPage update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PageRequest update) {
        Long userId = (Long) request.getAttribute("userId");
        CustomPage page = pageRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        page.setTitle(update.title());
        page.setSlug(ensureUniqueSlug(userId, slugify(update.slug()), page.getId()));
        page.setContent(sanitizer.sanitizeRichText(update.content()));
        page.setCustomCss(sanitizer.sanitizeCss(update.customCss()));
        page.setVisibility(resolveVisibility(update.visibility()));
        page.setPublishedAt(resolvePublishedAt(update.publishedAt(), page.getVisibility(), page.getPublishedAt()));
        page.setUpdatedAt(Instant.now());
        pageRepository.save(page);
        return page;
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        CustomPage page = pageRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        pageRepository.deleteById(page.getId());
    }

    private String ensureUniqueSlug(Long userId, String slug, Long existingId) {
        if (slug.isBlank()) {
            slug = "page-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String candidate = slug;
        while (true) {
            var existing = pageRepository.findBySlugAndUserId(candidate, userId);
            if (existing.isEmpty() || (existingId != null && existing.get().getId().equals(existingId))) {
                return candidate;
            }
            candidate = slug + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
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

    private String resolveVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return "DRAFT";
        }
        return visibility.toUpperCase(Locale.ROOT);
    }

    private Instant resolvePublishedAt(Instant requested, String visibility, Instant existing) {
        if (!"PUBLIC".equalsIgnoreCase(visibility)) {
            return requested;
        }
        return requested != null ? requested : (existing != null ? existing : Instant.now());
    }

    public record PageRequest(
            @NotBlank String title,
            @NotBlank String slug,
            @NotBlank String content,
            String customCss,
            String visibility,
            Instant publishedAt) {
    }
}
