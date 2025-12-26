package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.BlogComment;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.BlogPostTag;
import com.example.demo.entity.BlogTag;
import com.example.demo.repository.BlogCommentRepository;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.repository.BlogPostTagRepository;
import com.example.demo.repository.BlogTagRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin(origins = "*")
public class BlogController {
    private final BlogPostRepository postRepository;
    private final BlogTagRepository tagRepository;
    private final BlogPostTagRepository postTagRepository;
    private final BlogCommentRepository commentRepository;
    private final ContentSanitizer sanitizer;

    public BlogController(BlogPostRepository postRepository, BlogTagRepository tagRepository,
            BlogPostTagRepository postTagRepository, BlogCommentRepository commentRepository,
            ContentSanitizer sanitizer) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.commentRepository = commentRepository;
        this.sanitizer = sanitizer;
    }

    @GetMapping("/posts")
    public PagedResponse<BlogPostResponse> listPosts(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String visibility) {
        Long userId = (Long) request.getAttribute("userId");
        String visibilityFilter = visibility == null || visibility.isBlank() ? null : resolveVisibility(visibility);
        Page<BlogPost> posts = visibilityFilter == null
                ? postRepository.findByUserId(userId, PaginationUtils.toPageable(page, size, sort, "updatedAt"))
                : postRepository.findByUserIdAndVisibility(userId, visibilityFilter,
                        PaginationUtils.toPageable(page, size, sort, "updatedAt"));
        Map<Long, List<String>> tagsByPost = loadTags(posts.getContent());
        List<BlogPostResponse> items = posts.getContent().stream()
                .map(post -> BlogPostResponse.from(post, tagsByPost.getOrDefault(post.getId(), List.of())))
                .toList();
        return new PagedResponse<>(items, posts.getNumber(), posts.getSize(), posts.getTotalElements());
    }

    @GetMapping("/posts/{id}")
    public BlogPostResponse getPost(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        BlogPost post = postRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        List<String> tags = loadTags(List.of(post)).getOrDefault(post.getId(), List.of());
        return BlogPostResponse.from(post, tags);
    }

    @PostMapping("/posts")
    public BlogPostResponse createPost(HttpServletRequest request, @Valid @RequestBody BlogPostRequest postRequest) {
        Long userId = (Long) request.getAttribute("userId");
        BlogPost post = new BlogPost();
        post.setUserId(userId);
        post.setTitle(postRequest.title());
        post.setSlug(ensureUniqueSlug(userId, slugify(postRequest.title()), null));
        post.setContent(sanitizer.sanitizeRichText(postRequest.content()));
        post.setVisibility(resolveVisibility(postRequest.visibility()));
        post.setPublishedAt(resolvePublishedAt(postRequest.publishedAt(), post.getVisibility(), null));
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        syncTags(post.getId(), postRequest.tags());
        List<String> tags = loadTags(List.of(post)).getOrDefault(post.getId(), List.of());
        return BlogPostResponse.from(post, tags);
    }

    @PutMapping("/posts/{id}")
    public BlogPostResponse updatePost(HttpServletRequest request, @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        BlogPost post = postRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        post.setTitle(updateRequest.title());
        post.setSlug(ensureUniqueSlug(userId, slugify(updateRequest.title()), post.getId()));
        post.setContent(sanitizer.sanitizeRichText(updateRequest.content()));
        post.setVisibility(resolveVisibility(updateRequest.visibility()));
        post.setPublishedAt(resolvePublishedAt(updateRequest.publishedAt(), post.getVisibility(), post.getPublishedAt()));
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        syncTags(post.getId(), updateRequest.tags());
        List<String> tags = loadTags(List.of(post)).getOrDefault(post.getId(), List.of());
        return BlogPostResponse.from(post, tags);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        BlogPost post = postRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        postRepository.deleteById(post.getId());
        postTagRepository.deleteByPostId(post.getId());
    }

    @GetMapping("/posts/{id}/comments")
    public List<BlogComment> listComments(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        postRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return commentRepository.findByPostId(id);
    }

    @GetMapping("/comments")
    public PagedResponse<BlogComment> listCommentsForOwner(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status) {
        Long userId = (Long) request.getAttribute("userId");
        Page<BlogComment> comments = commentRepository.findByOwnerAndStatus(userId,
                status == null || status.isBlank() ? null : status,
                PaginationUtils.toPageable(page, size, sort, "createdAt"));
        return new PagedResponse<>(comments.getContent(), comments.getNumber(), comments.getSize(),
                comments.getTotalElements());
    }

    @PatchMapping("/comments/{id}/status")
    public void updateCommentStatus(HttpServletRequest request, @PathVariable Long id,
            @Valid @RequestBody CommentStatusRequest statusRequest) {
        Long userId = (Long) request.getAttribute("userId");
        BlogComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        postRepository.findByIdAndUserId(comment.getPostId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        comment.setStatus(statusRequest.status());
        commentRepository.save(comment);
    }

    @GetMapping("/tags")
    public List<BlogTag> listTags() {
        return tagRepository.findAll();
    }

    private void syncTags(Long postId, List<String> tags) {
        postTagRepository.deleteByPostId(postId);
        List<String> normalized = tags == null ? List.of()
                : tags.stream()
                        .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                        .filter(tag -> !tag.isBlank())
                        .distinct()
                        .toList();
        for (String tagName : normalized) {
            BlogTag tag = tagRepository.findByName(tagName).orElseGet(() -> createTag(tagName));
            BlogPostTag link = new BlogPostTag();
            link.setPostId(postId);
            link.setTagId(tag.getId());
            postTagRepository.save(link);
        }
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

    private BlogTag createTag(String name) {
        BlogTag tag = new BlogTag();
        tag.setName(name);
        tagRepository.save(tag);
        return tag;
    }

    private String ensureUniqueSlug(Long userId, String slug, Long existingId) {
        if (slug.isBlank()) {
            slug = "post-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String candidate = slug;
        while (true) {
            Optional<BlogPost> existing = postRepository.findBySlugAndUserId(candidate, userId);
            if (existing.isEmpty() || (existingId != null && existing.get().getId().equals(existingId))) {
                return candidate;
            }
            candidate = slug + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
    }

    private String slugify(String input) {
        String cleaned = input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return cleaned;
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

    public record BlogPostRequest(
            @NotBlank String title,
            @NotBlank String content,
            String visibility,
            Instant publishedAt,
            List<String> tags) {
    }

    public record BlogPostResponse(
            Long id,
            String title,
            String slug,
            String content,
            String visibility,
            Instant publishedAt,
            Instant createdAt,
            Instant updatedAt,
            List<String> tags) {
        public static BlogPostResponse from(BlogPost post, List<String> tags) {
            return new BlogPostResponse(post.getId(), post.getTitle(), post.getSlug(), post.getContent(),
                    post.getVisibility(), post.getPublishedAt(), post.getCreatedAt(), post.getUpdatedAt(), tags);
        }
    }

    public record CommentStatusRequest(@NotBlank String status) {
    }
}
