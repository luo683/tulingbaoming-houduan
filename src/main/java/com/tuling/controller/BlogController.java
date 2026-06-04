package com.tuling.controller;

import com.tuling.entity.BlogComment;
import com.tuling.entity.BlogPost;
import com.tuling.service.BlogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Blog REST Controller
 * Public endpoints under /api/blog/posts
 * Admin endpoints under /api/blog/admin
 */
@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private static final Logger log = LoggerFactory.getLogger(BlogController.class);

    @Autowired
    private BlogService blogService;

    // ── Public: Blog Posts ──

    /**
     * Get all blog posts, optionally filtered by tag.
     */
    @GetMapping("/posts")
    public ResponseEntity<List<BlogPost>> getPosts(@RequestParam(defaultValue = "") String tag) {
        log.info("GET /api/blog/posts, tag={}", tag);
        List<BlogPost> posts;
        if (tag != null && !tag.trim().isEmpty()) {
            posts = blogService.getPostsByTag(tag.trim());
        } else {
            posts = blogService.getPublishedPosts();
        }
        return ResponseEntity.ok(posts);
    }

    /**
     * Get a single blog post by its slug.
     */
    @GetMapping("/posts/{slug}")
    public ResponseEntity<BlogPost> getPost(@PathVariable String slug) {
        log.info("GET /api/blog/posts/{}", slug);
        try {
            BlogPost post = blogService.getPostBySlug(slug);
            return ResponseEntity.ok(post);
        } catch (NoSuchElementException e) {
            log.warn("Post not found: {}", slug);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new blog post (public submission).
     */
    @PostMapping("/posts")
    public ResponseEntity<BlogPost> createPost(@RequestBody BlogPost post) {
        log.info("POST /api/blog/posts, title={}", post.getTitle());
        BlogPost created = blogService.createPost(post);
        return ResponseEntity.ok(created);
    }

    /**
     * Update an existing blog post.
     */
    @PutMapping("/posts/{id}")
    public ResponseEntity<BlogPost> updatePost(@PathVariable Long id, @RequestBody BlogPost update) {
        log.info("PUT /api/blog/posts/{}", id);
        try {
            BlogPost updated = blogService.updatePost(id, update);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Post not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a blog post.
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("DELETE /api/blog/posts/{}", id);
        try {
            blogService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Post not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ── Public: Comments ──

    /**
     * Get all comments for a blog post.
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<BlogComment>> getComments(@PathVariable Long postId) {
        log.info("GET /api/blog/posts/{}/comments", postId);
        List<BlogComment> comments = blogService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Add a comment to a blog post.
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<BlogComment> addComment(@PathVariable Long postId, @RequestBody BlogComment comment) {
        log.info("POST /api/blog/posts/{}/comments, nickname={}", postId, comment.getNickname());
        BlogComment saved = blogService.addComment(postId, comment);
        return ResponseEntity.ok(saved);
    }

    // ── Admin: Post Management ──

    /**
     * Admin: Set the status of a blog post (e.g., published, draft, rejected).
     */
    @PutMapping("/admin/posts/{id}/status")
    public ResponseEntity<BlogPost> setStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("PUT /api/blog/admin/posts/{}/status -> {}", id, status);
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            BlogPost updated = blogService.setStatus(id, status.trim());
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Post not found for status update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete a blog post.
     */
    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<Void> adminDeletePost(@PathVariable Long id) {
        log.info("DELETE /api/blog/admin/posts/{}", id);
        try {
            blogService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Post not found for admin deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
