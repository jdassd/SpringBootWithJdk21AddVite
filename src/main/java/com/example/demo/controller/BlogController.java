package com.example.demo.controller;

import com.example.demo.entity.BlogComment;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.BlogPostTag;
import com.example.demo.entity.BlogTag;
import com.example.demo.repository.BlogCommentRepository;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.repository.BlogPostTagRepository;
import com.example.demo.repository.BlogTagRepository;
import com.example.demo.security.ContentSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
    public List<BlogPost> listPosts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return postRepository.findByUserId(userId);
    }

    @GetMapping("/posts/public")
    public List<BlogPost> listPublished() {
        // Public blog view could be global or per user.
        // For a shared blog service, it might need to filter by status AND maybe a
        // specific user if requested.
        // For now, we keep it simple but ensure status is honored.
        return postRepository.findByStatus("PUBLISHED");
    }

    @GetMapping("/posts/{id}")
    public BlogPost getPost(@PathVariable Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @PostMapping("/posts")
    public BlogPost createPost(HttpServletRequest request, @Valid @RequestBody BlogPostRequest postRequest) {
        Long userId = (Long) request.getAttribute("userId");
        BlogPost post = new BlogPost();
        post.setUserId(userId);
        post.setTitle(postRequest.title());
        post.setSlug(slugify(postRequest.title()));
        post.setContent(sanitizer.sanitizeRichText(postRequest.content()));
        post.setStatus(postRequest.status());
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        syncTags(post.getId(), postRequest.tags());
        return post;
    }

    @PutMapping("/posts/{id}")
    public BlogPost updatePost(@PathVariable Long id, @Valid @RequestBody BlogPostRequest request) {
        BlogPost post = postRepository.findById(id).orElseThrow();
        post.setTitle(request.title());
        post.setSlug(slugify(request.title()));
        post.setContent(sanitizer.sanitizeRichText(request.content()));
        post.setStatus(request.status());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        syncTags(post.getId(), request.tags());
        return post;
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        postTagRepository.deleteByPostId(id);
    }

    @GetMapping("/posts/{id}/comments")
    public List<BlogComment> listComments(@PathVariable Long id) {
        return commentRepository.findByPostId(id);
    }

    @PostMapping("/posts/{id}/comments")
    public BlogComment createComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        BlogComment comment = new BlogComment();
        comment.setPostId(id);
        comment.setAuthor(sanitizer.sanitizePlainText(request.author()));
        comment.setContent(sanitizer.sanitizeRichText(request.content()));
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        return comment;
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

    private BlogTag createTag(String name) {
        BlogTag tag = new BlogTag();
        tag.setName(name);
        tagRepository.save(tag);
        return tag;
    }

    private String slugify(String input) {
        String cleaned = input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        // Use random UUID short part instead of timestamp for better safety if titles
        // collide.
        return cleaned.isBlank() ? "post-" + UUID.randomUUID().toString().substring(0, 8) : cleaned;
    }

    public record BlogPostRequest(
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String status,
            @NotEmpty List<String> tags) {
    }

    public record CommentRequest(
            @NotBlank String author,
            @NotBlank String content) {
    }
}
