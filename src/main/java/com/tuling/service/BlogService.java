package com.tuling.service;

import com.tuling.entity.BlogComment;
import com.tuling.entity.BlogPost;
import com.tuling.repository.BlogCommentRepository;
import com.tuling.repository.BlogPostRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCommentRepository blogCommentRepository;

    /**
     * Get all published blog posts, newest first.
     */
    public List<BlogPost> getPublishedPosts() {
        log.debug("Fetching all published posts");
        return blogPostRepository.findByStatusOrderByCreatedAtDesc("published");
    }

    /**
     * Get published blog posts filtered by a tag (comma-separated or single tag).
     */
    public List<BlogPost> getPostsByTag(String tag) {
        log.debug("Fetching published posts by tag: {}", tag);
        List<BlogPost> all = blogPostRepository.findByStatusOrderByCreatedAtDesc("published");
        return all.stream()
                .filter(post -> post.getTags() != null && post.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * Get a single blog post by its slug.
     */
    public BlogPost getPostBySlug(String slug) {
        log.debug("Fetching post by slug: {}", slug);
        return blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Blog post not found with slug: " + slug));
    }

    /**
     * Create a new blog post. Sets status to published by default.
     */
    @Transactional
    public BlogPost createPost(BlogPost post) {
        log.info("Creating new blog post: {}", post.getTitle());
        post.setStatus("published");
        BlogPost saved = blogPostRepository.save(post);
        log.info("Blog post created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing blog post. Only non-null fields from the update are applied.
     */
    @Transactional
    public BlogPost updatePost(Long id, BlogPost update) {
        log.info("Updating blog post id: {}", id);
        BlogPost existing = blogPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Blog post not found with id: " + id));

        if (update.getTitle() != null) {
            existing.setTitle(update.getTitle());
        }
        if (update.getSlug() != null) {
            existing.setSlug(update.getSlug());
        }
        if (update.getContent() != null) {
            existing.setContent(update.getContent());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getTags() != null) {
            existing.setTags(update.getTags());
        }
        if (update.getCoverEmoji() != null) {
            existing.setCoverEmoji(update.getCoverEmoji());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        if (update.getAuthorName() != null) {
            existing.setAuthorName(update.getAuthorName());
        }
        if (update.getViewCount() > 0) {
            existing.setViewCount(update.getViewCount());
        }

        BlogPost saved = blogPostRepository.save(existing);
        log.info("Blog post updated: {}", saved.getId());
        return saved;
    }

    /**
     * Delete a blog post by id.
     */
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting blog post id: {}", id);
        if (!blogPostRepository.existsById(id)) {
            throw new NoSuchElementException("Blog post not found with id: " + id);
        }
        blogPostRepository.deleteById(id);
        log.info("Blog post deleted: {}", id);
    }

    /**
     * Admin: Set the status of a blog post (e.g. published, draft, rejected).
     */
    @Transactional
    public BlogPost setStatus(Long id, String status) {
        log.info("Setting status '{}' on blog post id: {}", status, id);
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Blog post not found with id: " + id));
        post.setStatus(status);
        BlogPost saved = blogPostRepository.save(post);
        log.info("Blog post {} status set to: {}", id, status);
        return saved;
    }

    // ── Comments ──

    /**
     * Get all comments for a blog post, newest first.
     */
    public List<BlogComment> getComments(Long postId) {
        log.debug("Fetching comments for post id: {}", postId);
        return blogCommentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    /**
     * Add a comment to a blog post.
     */
    @Transactional
    public BlogComment addComment(Long postId, BlogComment comment) {
        log.info("Adding comment to post id: {}", postId);
        comment.setPostId(postId);
        BlogComment saved = blogCommentRepository.save(comment);
        log.info("Comment added with id: {}", saved.getId());
        return saved;
    }

    // ── Search ──

    /**
     * Search blog posts by keyword in title, content, or tags.
     */
    public List<BlogPost> search(String keyword) {
        log.debug("Searching blog posts with keyword: {}", keyword);
        return blogPostRepository.searchByKeyword(keyword);
    }
}
