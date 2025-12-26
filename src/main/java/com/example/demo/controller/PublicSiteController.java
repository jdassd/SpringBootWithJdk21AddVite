package com.example.demo.controller;

import com.example.demo.entity.Site;
import com.example.demo.service.SiteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/{siteKey}")
@CrossOrigin(origins = "*")
public class PublicSiteController {
    private final SiteService siteService;

    public PublicSiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/site")
    public SiteController.SiteResponse getSite(@PathVariable String siteKey) {
        Site site = siteService.resolveSite(siteKey);
        return SiteController.SiteResponse.from(site);
    }
}
