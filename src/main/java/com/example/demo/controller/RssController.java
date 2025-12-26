package com.example.demo.controller;

import com.example.demo.entity.BlogPost;
import com.example.demo.entity.Site;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.service.SiteService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@RestController
public class RssController {
    private final BlogPostRepository postRepository;
    private final SiteService siteService;

    public RssController(BlogPostRepository postRepository, SiteService siteService) {
        this.postRepository = postRepository;
        this.siteService = siteService;
    }

    @GetMapping(value = "/api/public/{siteKey}/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss(@PathVariable String siteKey) {
        Site site = siteService.resolveSite(siteKey);
        Instant now = Instant.now();
        List<BlogPost> posts = postRepository.findByUserIdAndVisibility(site.getOwnerUserId(), "PUBLIC")
                .stream()
                .filter(post -> post.getPublishedAt() == null || !post.getPublishedAt().isAfter(now))
                .sorted(Comparator.comparing(BlogPost::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        String items = posts.stream()
            .map(post -> """
                <item>
                  <title>%s</title>
                  <link>/posts/%s</link>
                  <guid>%s</guid>
                  <pubDate>%s</pubDate>
                </item>
                """.formatted(
                escape(post.getTitle()),
                escape(post.getSlug()),
                escape(post.getSlug()),
                post.getCreatedAt() == null ? "" : DateTimeFormatter.RFC_1123_DATE_TIME.format(
                    (post.getPublishedAt() == null ? post.getCreatedAt() : post.getPublishedAt()).atZone(ZoneId.of("UTC"))
                )
            ))
            .reduce("", String::concat);
        return """
            <?xml version="1.0" encoding="UTF-8" ?>
            <rss version="2.0">
              <channel>
                <title>Blog Feed</title>
                <link>/</link>
                <description>Latest posts</description>
                %s
              </channel>
            </rss>
            """.formatted(items);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
