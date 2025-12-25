package com.example.demo.controller;

import com.example.demo.entity.CustomPage;
import com.example.demo.repository.CustomPageRepository;
import com.example.demo.security.ContentSanitizer;
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
    public List<CustomPage> listPages() {
        return pageRepository.findAll();
    }

    @GetMapping("/public")
    public List<CustomPage> listPublicPages() {
        return pageRepository.findAll();
    }

    @GetMapping("/public/{slug}")
    public CustomPage getBySlug(@PathVariable String slug) {
        return pageRepository.findBySlug(slug).orElse(null);
    }

    @PostMapping
    public CustomPage create(@Valid @RequestBody PageRequest request) {
        CustomPage page = new CustomPage();
        page.setId(System.currentTimeMillis());
        page.setTitle(request.title());
        page.setSlug(request.slug());
        page.setContent(sanitizer.sanitizeRichText(request.content()));
        page.setCustomCss(sanitizer.sanitizeCss(request.customCss()));
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
        String customCss
    ) {
    }
}
