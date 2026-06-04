package com.tuling.controller;

import com.tuling.entity.BlogPost;
import com.tuling.entity.Event;
import com.tuling.entity.Faq;
import com.tuling.entity.Resource;
import com.tuling.service.BlogService;
import com.tuling.service.EventService;
import com.tuling.service.FaqService;
import com.tuling.service.ResourceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Search REST Controller
 * Provides cross-entity keyword search across all content types.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private BlogService blogService;

    @Autowired
    private EventService eventService;

    @Autowired
    private FaqService faqService;

    @Autowired
    private ResourceService resourceService;

    /**
     * Search all content types by keyword.
     * Searches across: blog posts, events, FAQs, and resources.
     *
     * @param q the search keyword
     * @return a map with keys: posts, events, faqs, resources
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        log.info("GET /api/search?q={}", q);

        if (q == null || q.trim().isEmpty()) {
            log.warn("Empty search query");
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("posts", Collections.emptyList());
            emptyResult.put("events", Collections.emptyList());
            emptyResult.put("faqs", Collections.emptyList());
            emptyResult.put("resources", Collections.emptyList());
            return ResponseEntity.ok(emptyResult);
        }

        String keyword = q.trim().toLowerCase();

        // Search blog posts using BlogService's built-in search
        List<BlogPost> posts = blogService.search(keyword);
        log.debug("Blog search '{}' returned {} results", keyword, posts.size());

        // Search events by filtering title and description
        List<Event> events;
        try {
            events = eventService.getAllEvents().stream()
                    .filter(e -> {
                        String title = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
                        String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
                        return title.contains(keyword) || desc.contains(keyword);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching events: {}", e.getMessage(), e);
            events = Collections.emptyList();
        }
        log.debug("Event search '{}' returned {} results", keyword, events.size());

        // Search FAQs by filtering question and answer text
        List<Faq> faqs;
        try {
            faqs = faqService.getAllFaqs().stream()
                    .filter(f -> {
                        String question = f.getQuestion() != null ? f.getQuestion().toLowerCase() : "";
                        String answer = f.getAnswer() != null ? f.getAnswer().toLowerCase() : "";
                        return question.contains(keyword) || answer.contains(keyword);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching FAQs: {}", e.getMessage(), e);
            faqs = Collections.emptyList();
        }
        log.debug("FAQ search '{}' returned {} results", keyword, faqs.size());

        // Search resources by filtering title and description
        List<Resource> resources;
        try {
            resources = resourceService.getPublishedResources().stream()
                    .filter(r -> {
                        String title = r.getTitle() != null ? r.getTitle().toLowerCase() : "";
                        String desc = r.getDescription() != null ? r.getDescription().toLowerCase() : "";
                        return title.contains(keyword) || desc.contains(keyword);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching resources: {}", e.getMessage(), e);
            resources = Collections.emptyList();
        }
        log.debug("Resource search '{}' returned {} results", keyword, resources.size());

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("events", events);
        result.put("faqs", faqs);
        result.put("resources", resources);

        int totalCount = posts.size() + events.size() + faqs.size() + resources.size();
        result.put("totalCount", totalCount);

        log.info("Search '{}' completed: {} total results (posts={}, events={}, faqs={}, resources={})",
                keyword, totalCount, posts.size(), events.size(), faqs.size(), resources.size());

        return ResponseEntity.ok(result);
    }
}
