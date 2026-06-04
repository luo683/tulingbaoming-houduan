package com.tuling.service;

import com.tuling.entity.Resource;
import com.tuling.entity.ResourceLike;
import com.tuling.repository.ResourceLikeRepository;
import com.tuling.repository.ResourceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceLikeRepository resourceLikeRepository;

    /**
     * Get all published resources, newest first.
     */
    public List<Resource> getPublishedResources() {
        log.debug("Fetching published resources");
        return resourceRepository.findByStatusOrderByCreatedAtDesc("published");
    }

    /**
     * Get resources filtered by category, newest first.
     */
    public List<Resource> getResourcesByCategory(String category) {
        log.debug("Fetching resources by category: {}", category);
        return resourceRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    /**
     * Create a new resource.
     */
    @Transactional
    public Resource createResource(Resource resource) {
        log.info("Creating new resource: {}", resource.getTitle());
        Resource saved = resourceRepository.save(resource);
        log.info("Resource created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Get like info for a resource + user combo.
     */
    public Map<String, Object> getLikeInfo(Long resourceId, Long userId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new NoSuchElementException("Resource not found: " + resourceId));
        boolean liked = hasLiked(resourceId, userId);
        long count = getLikeCount(resourceId);
        Map<String, Object> info = new HashMap<>();
        info.put("liked", liked);
        info.put("likeCount", count);
        info.put("resourceId", resourceId);
        return info;
    }

    /**
     * Delete a resource by id.
     */
    @Transactional
    public void deleteResource(Long id) {
        log.info("Deleting resource id: {}", id);
        if (!resourceRepository.existsById(id)) {
            throw new NoSuchElementException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
        log.info("Resource deleted: {}", id);
    }

    // ── Likes ──

    /**
     * Check if a user has already liked a resource.
     */
    public boolean hasLiked(Long resourceId, Long userId) {
        return resourceLikeRepository.findByResourceIdAndUserId(resourceId, userId).isPresent();
    }

    /**
     * Get the total like count for a resource.
     */
    public long getLikeCount(Long resourceId) {
        return resourceLikeRepository.countByResourceId(resourceId);
    }

    /**
     * Like a resource. Idempotent: if already liked, does nothing.
     */
    @Transactional
    public void likeResource(Long resourceId, Long userId) {
        log.info("User {} liking resource {}", userId, resourceId);
        if (!hasLiked(resourceId, userId)) {
            ResourceLike like = new ResourceLike(resourceId, userId);
            resourceLikeRepository.save(like);
            updateLikeCount(resourceId);
            log.info("User {} liked resource {}", userId, resourceId);
        } else {
            log.debug("User {} already liked resource {}", userId, resourceId);
        }
    }

    /**
     * Unlike a resource. Idempotent: if not liked, does nothing.
     */
    @Transactional
    public void unlikeResource(Long resourceId, Long userId) {
        log.info("User {} unliking resource {}", userId, resourceId);
        resourceLikeRepository.deleteByResourceIdAndUserId(resourceId, userId);
        updateLikeCount(resourceId);
        log.info("User {} unliked resource {}", userId, resourceId);
    }

    /**
     * Synchronize the like count on a resource with the actual number of likes.
     */
    private void updateLikeCount(Long resourceId) {
        long count = resourceLikeRepository.countByResourceId(resourceId);
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource != null) {
            resource.setLikeCount((int) count);
            resourceRepository.save(resource);
            log.debug("Resource {} like count updated to {}", resourceId, count);
        }
    }
}
