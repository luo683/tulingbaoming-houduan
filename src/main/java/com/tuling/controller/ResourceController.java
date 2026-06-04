package com.tuling.controller;

import com.tuling.entity.Resource;
import com.tuling.service.ResourceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Resource REST Controller
 * Public endpoints under /api/resources
 * Admin endpoints under /api/resources/admin
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceService resourceService;

    // ── Public Endpoints ──

    /**
     * Get all resources, optionally filtered by category.
     */
    @GetMapping
    public ResponseEntity<List<Resource>> getResources(@RequestParam(defaultValue = "") String category) {
        log.info("GET /api/resources, category={}", category);
        List<Resource> resources;
        if (category != null && !category.trim().isEmpty()) {
            resources = resourceService.getResourcesByCategory(category.trim());
        } else {
            resources = resourceService.getPublishedResources();
        }
        return ResponseEntity.ok(resources);
    }

    /**
     * Get like information for a specific resource and user.
     * Returns whether the user has liked the resource and the total like count.
     */
    @GetMapping("/{id}/likes")
    public ResponseEntity<Map<String, Object>> getLikeInfo(@PathVariable Long id, @RequestParam Long userId) {
        log.info("GET /api/resources/{}/likes?userId={}", id, userId);
        try {
            Map<String, Object> likeInfo = resourceService.getLikeInfo(id, userId);
            return ResponseEntity.ok(likeInfo);
        } catch (NoSuchElementException e) {
            log.warn("Resource not found for like info: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Like a resource.
     * Request body should contain {"userId": <number>}
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeResource(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        log.info("POST /api/resources/{}/like, userId={}", id, userId);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            resourceService.likeResource(id, userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Resource not found for like: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Duplicate like: resourceId={}, userId={}", id, userId);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Unlike a resource (remove the user's like).
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeResource(@PathVariable Long id, @RequestParam Long userId) {
        log.info("DELETE /api/resources/{}/like?userId={}", id, userId);
        try {
            resourceService.unlikeResource(id, userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Resource not found for unlike: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new resource.
     */
    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        log.info("POST /api/resources, title={}", resource.getTitle());
        Resource created = resourceService.createResource(resource);
        return ResponseEntity.ok(created);
    }

    // ── Admin Endpoints ──

    /**
     * Admin: Delete a resource.
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        log.info("DELETE /api/resources/admin/{}", id);
        try {
            resourceService.deleteResource(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Resource not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
