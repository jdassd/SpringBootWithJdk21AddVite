package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.SearchEngine;
import com.example.demo.entity.Site;
import com.example.demo.repository.SearchEngineRepository;
import com.example.demo.service.SiteService;
import com.example.demo.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/{siteKey}/search-engines")
@CrossOrigin(origins = "*")
public class PublicSearchEngineController {
    private final SiteService siteService;
    private final SearchEngineRepository repository;

    public PublicSearchEngineController(SiteService siteService, SearchEngineRepository repository) {
        this.siteService = siteService;
        this.repository = repository;
    }

    @GetMapping
    public PagedResponse<SearchEngine> list(@PathVariable String siteKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Site site = siteService.resolveSite(siteKey);
        Page<SearchEngine> engines = repository.findByUserId(site.getOwnerUserId(),
                PaginationUtils.toPageable(page, size, sort, "name"));
        return new PagedResponse<>(engines.getContent(), engines.getNumber(), engines.getSize(), engines.getTotalElements());
    }
}
