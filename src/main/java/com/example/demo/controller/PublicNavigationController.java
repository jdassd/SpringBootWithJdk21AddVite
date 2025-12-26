package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.NavigationLink;
import com.example.demo.entity.Site;
import com.example.demo.repository.NavigationLinkRepository;
import com.example.demo.service.SiteService;
import com.example.demo.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/{siteKey}/navigation")
@CrossOrigin(origins = "*")
public class PublicNavigationController {
    private final SiteService siteService;
    private final NavigationLinkRepository linkRepository;

    public PublicNavigationController(SiteService siteService, NavigationLinkRepository linkRepository) {
        this.siteService = siteService;
        this.linkRepository = linkRepository;
    }

    @GetMapping
    public PagedResponse<NavigationLink> listPublicLinks(@PathVariable String siteKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Site site = siteService.resolveSite(siteKey);
        Page<NavigationLink> links = linkRepository.findByUserId(site.getOwnerUserId(),
                PaginationUtils.toPageable(page, size, sort, "sortOrder"));
        return new PagedResponse<>(links.getContent(), links.getNumber(), links.getSize(), links.getTotalElements());
    }
}
