package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.BlogComment;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.BlogPostTag;
import com.example.demo.entity.BlogTag;
import com.example.demo.entity.Site;
import com.example.demo.repository.BlogCommentRepository;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.repository.BlogPostTagRepository;
import com.example.demo.repository.BlogTagRepository;
import com.example.demo.security.ContentSanitizer;
import com.example.demo.service.SiteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/{siteKey}/blog")
@CrossOrigin(origins = "*")
public class PublicBlogController {
    private final SiteService siteService;
    private final BlogPostRepository postRepository;
    private final BlogTagRepository tagRepository;
    private final BlogPostTagRepository postTagRepository;
    private final BlogCommentRepository commentRepository;
    private final ContentSanitizer sanitizer;

    public PublicBlogController(SiteService siteService, BlogPostRepository postRepository, BlogTagRepository tagRepository,
            BlogPostTagRepository postTagRepository, BlogCommentRepository commentRepository, ContentSanitizer sanitizer) {
        this.siteService = siteService;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.commentRepository = commentRepository;
        this.sanitizer = sanitizer;
    }

    @GetMapping("/posts")
    public PagedResponse<BlogController.BlogPostResponse> listPublicPosts(@PathVariable String siteKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        Site site = siteService.resolveSite(siteKey);
        List<BlogPost> posts = postRepository.findByUserIdAndVisibility(site.getOwnerUserId(), "PUBLIC");
        Instant now = Instant.now();
        posts = posts.stream()
                .filter(post -> post.getPublishedAt() == null || !post.getPublishedAt().isAfter(now))
                .sorted(Comparator.comparing(BlogPost::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        Map<Long, List<String>> tagsByPost = loadTags(posts);
        if (tag != null && !tag.isBlank()) {
            String normalized = tag.trim().toLowerCase(Locale.ROOT);
            posts = posts.stream()
                    .filter(post -> tagsByPost.getOrDefault(post.getId(), List.of()).contains(normalized))
                    .toList();
        }
        if (from != null || to != null) {
            Instant start = from == null ? Instant.MIN : from.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = to == null ? Instant.MAX : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            posts = posts.stream()
                    .filter(post -> post.getPublishedAt() != null
                            && !post.getPublishedAt().isBefore(start)
                            && post.getPublishedAt().isBefore(end))
                    .toList();
        }
        int total = posts.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<BlogPost> pageItems = posts.subList(fromIndex, toIndex);
        List<BlogController.BlogPostResponse> items = pageItems.stream()
                .map(post -> BlogController.BlogPostResponse.from(post, tagsByPost.getOrDefault(post.getId(), List.of())))
                .toList();
        return new PagedResponse<>(items, page, size, total);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<BlogComment> listApprovedComments(@PathVariable String siteKey, @PathVariable Long postId) {
        Site site = siteService.resolveSite(siteKey);
        postRepository.findByIdAndUserId(postId, site.getOwnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return commentRepository.findByPostIdAndStatus(postId, "APPROVED");
    }

    @PostMapping("/posts/{postId}/comments")
    public BlogComment createComment(@PathVariable String siteKey, @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        Site site = siteService.resolveSite(siteKey);
        postRepository.findByIdAndUserId(postId, site.getOwnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        BlogComment comment = new BlogComment();
        comment.setPostId(postId);
        comment.setAuthor(sanitizer.sanitizePlainText(request.author()));
        comment.setContent(sanitizer.sanitizeRichText(request.content()));
        comment.setStatus("PENDING");
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        return comment;
    }

    @GetMapping("/tags")
    public List<BlogTag> listTags(@PathVariable String siteKey) {
        siteService.resolveSite(siteKey);
        return tagRepository.findAll();
    }

    private Map<Long, List<String>> loadTags(List<BlogPost> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        List<Long> postIds = posts.stream().map(BlogPost::getId).toList();
        List<BlogPostTag> links = postTagRepository.findByPostIdIn(postIds);
        Set<Long> tagIds = links.stream().map(BlogPostTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> tagNames = tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(BlogTag::getId, BlogTag::getName));
        Map<Long, List<String>> result = new HashMap<>();
        for (BlogPostTag link : links) {
            String tagName = tagNames.get(link.getTagId());
            if (tagName == null) {
                continue;
            }
            result.computeIfAbsent(link.getPostId(), key -> new ArrayList<>()).add(tagName);
        }
        return result;
    }

    public record CommentRequest(@NotBlank String author, @NotBlank String content) {
    }
}
