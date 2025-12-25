package com.example.demo.controller;

import com.example.demo.entity.CustomPage;
import com.example.demo.repository.CustomPageRepository;
import com.example.demo.security.ContentSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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
    public List<CustomPage> listPages(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pageRepository.findByUserId(userId);
    }

    @GetMapping("/public")
    public List<CustomPage> listPublicPages(HttpServletRequest request) {
        return listPages(request);
    }

    @GetMapping("/public/{slug}")
    public CustomPage getBySlug(@PathVariable String slug) {
        // NOTE: slugs might need to be scoped by userId as well if multiple users use
        // the same slug.
        // For now, we remain parity but fix the lack of isolation in lists.
        return pageRepository.findBySlug(slug).orElse(null);
    }

    @PostMapping
    public CustomPage create(HttpServletRequest request, @Valid @RequestBody PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        CustomPage page = new CustomPage();
        page.setUserId(userId);
        page.setTitle(pageRequest.title());
        page.setSlug(pageRequest.slug());
        page.setContent(sanitizer.sanitizeRichText(pageRequest.content()));
        page.setCustomCss(sanitizer.sanitizeCss(pageRequest.customCss()));
        page.setCreatedAt(Instant.now());
        page.setUpdatedAt(Instant.now());
        pageRepository.save(page);
        return page;
    }

    @PutMapping("/{id}")
    public CustomPage update(@PathVariable Long id, @Valid @RequestBody PageRequest request) {
        CustomPage page = pageRepository.findById(id).orElseThrow();
        page.setTitle(request.title());
        page.setSlug(request.slug());
        page.setContent(sanitizer.sanitizeRichText(request.content()));
        page.setCustomCss(sanitizer.sanitizeCss(request.customCss()));
        page.setUpdatedAt(Instant.now());
        pageRepository.save(page);
        return page;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        pageRepository.deleteById(id);
    }

    public record PageRequest(
            @NotBlank String title,
            @NotBlank String slug,
            @NotBlank String content,
            String customCss) {
    }
}
