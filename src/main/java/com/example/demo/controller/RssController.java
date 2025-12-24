package com.example.demo.controller;

import com.example.demo.entity.BlogPost;
import com.example.demo.repository.BlogPostRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

@RestController
public class RssController {
    private final BlogPostRepository postRepository;

    public RssController(BlogPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping(value = "/api/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss() {
        List<BlogPost> posts = postRepository.findByStatus("PUBLISHED");
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
                    post.getCreatedAt().atZone(ZoneId.of("UTC"))
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
