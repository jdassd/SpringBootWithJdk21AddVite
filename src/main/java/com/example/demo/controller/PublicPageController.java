package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.CustomPage;
import com.example.demo.entity.Site;
import com.example.demo.repository.CustomPageRepository;
import com.example.demo.service.SiteService;
import com.example.demo.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/public/{siteKey}/pages")
@CrossOrigin(origins = "*")
public class PublicPageController {
    private final SiteService siteService;
    private final CustomPageRepository pageRepository;

    public PublicPageController(SiteService siteService, CustomPageRepository pageRepository) {
        this.siteService = siteService;
        this.pageRepository = pageRepository;
    }

    @GetMapping
    public PagedResponse<CustomPage> listPages(@PathVariable String siteKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Site site = siteService.resolveSite(siteKey);
        Page<CustomPage> pages = pageRepository.findByUserIdAndVisibility(site.getOwnerUserId(), "PUBLIC",
                PaginationUtils.toPageable(page, size, sort, "updatedAt"));
        return new PagedResponse<>(pages.getContent(), pages.getNumber(), pages.getSize(), pages.getTotalElements());
    }

    @GetMapping("/{slug}")
    public CustomPage getBySlug(@PathVariable String siteKey, @PathVariable String slug) {
        Site site = siteService.resolveSite(siteKey);
        CustomPage page = pageRepository.findBySlugAndUserId(slug, site.getOwnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        if (!"PUBLIC".equalsIgnoreCase(page.getVisibility())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }
        if (page.getPublishedAt() != null && page.getPublishedAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }
        return page;
    }
}
