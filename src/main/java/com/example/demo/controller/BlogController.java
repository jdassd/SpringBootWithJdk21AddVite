package com.example.demo.controller;

import com.example.demo.entity.BlogComment;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.BlogPostTag;
import com.example.demo.entity.BlogTag;
import com.example.demo.repository.BlogCommentRepository;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.repository.BlogPostTagRepository;
import com.example.demo.repository.BlogTagRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin(origins = "*")
public class BlogController {
    private final BlogPostRepository postRepository;
    private final BlogTagRepository tagRepository;
    private final BlogPostTagRepository postTagRepository;
    private final BlogCommentRepository commentRepository;

    public BlogController(BlogPostRepository postRepository, BlogTagRepository tagRepository,
                          BlogPostTagRepository postTagRepository, BlogCommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/posts")
    public List<BlogPost> listPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/posts/public")
    public List<BlogPost> listPublished() {
        return postRepository.findByStatus("PUBLISHED");
    }

    @GetMapping("/posts/{id}")
    public BlogPost getPost(@PathVariable Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @PostMapping("/posts")
    public BlogPost createPost(@Valid @RequestBody BlogPostRequest request) {
        BlogPost post = new BlogPost();
        post.setId(System.currentTimeMillis());
        post.setTitle(request.title());
        post.setSlug(slugify(request.title()));
        post.setContent(request.content());
        post.setStatus(request.status());
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        syncTags(post.getId(), request.tags());
        return post;
    }

    @PutMapping("/posts/{id}")
    public BlogPost updatePost(@PathVariable Long id, @Valid @RequestBody BlogPostRequest request) {
        BlogPost post = postRepository.findById(id).orElseThrow();
        post.setTitle(request.title());
        post.setSlug(slugify(request.title()));
        post.setContent(request.content());
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
        comment.setId(System.currentTimeMillis());
        comment.setPostId(id);
        comment.setAuthor(request.author());
        comment.setContent(request.content());
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
        List<String> normalized = tags == null ? List.of() : tags.stream()
            .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
            .filter(tag -> !tag.isBlank())
            .distinct()
            .toList();
        for (String tagName : normalized) {
            BlogTag tag = tagRepository.findByName(tagName).orElseGet(() -> createTag(tagName));
            BlogPostTag link = new BlogPostTag();
            link.setId(System.nanoTime());
            link.setPostId(postId);
            link.setTagId(tag.getId());
            postTagRepository.save(link);
        }
    }

    private BlogTag createTag(String name) {
        BlogTag tag = new BlogTag();
        tag.setId(System.nanoTime());
        tag.setName(name);
        tagRepository.save(tag);
        return tag;
    }

    private String slugify(String input) {
        String cleaned = input.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9\\s-]", "")
            .trim()
            .replaceAll("\\s+", "-");
        return cleaned.isBlank() ? "post" + System.currentTimeMillis() : cleaned;
    }

    public record BlogPostRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String status,
        @NotEmpty List<String> tags
    ) {
    }

    public record CommentRequest(
        @NotBlank String author,
        @NotBlank String content
    ) {
    }
}
