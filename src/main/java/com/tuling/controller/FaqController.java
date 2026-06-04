package com.tuling.controller;

import com.tuling.entity.Faq;
import com.tuling.entity.QuestionReply;
import com.tuling.entity.UserQuestion;
import com.tuling.service.FaqService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * FAQ REST Controller
 * Public endpoints under /api/faqs
 * Admin endpoints under /api/faqs/admin
 */
@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private static final Logger log = LoggerFactory.getLogger(FaqController.class);

    @Autowired
    private FaqService faqService;

    // ── Public: Official FAQs ──

    /**
     * Get all official FAQs, ordered by sort order.
     */
    @GetMapping
    public ResponseEntity<List<Faq>> getAllFaqs() {
        log.info("GET /api/faqs");
        List<Faq> faqs = faqService.getAllFaqs();
        return ResponseEntity.ok(faqs);
    }

    // ── Public: User Questions ──

    /**
     * Get all user-submitted questions.
     */
    @GetMapping("/questions")
    public ResponseEntity<List<UserQuestion>> getQuestions() {
        log.info("GET /api/faqs/questions");
        List<UserQuestion> questions = faqService.getQuestions();
        return ResponseEntity.ok(questions);
    }

    /**
     * Submit a new question.
     */
    @PostMapping("/ask")
    public ResponseEntity<UserQuestion> askQuestion(@RequestBody UserQuestion question) {
        log.info("POST /api/faqs/ask, title={}", question.getTitle());
        UserQuestion saved = faqService.askQuestion(question);
        return ResponseEntity.ok(saved);
    }

    /**
     * Get all replies for a specific question.
     */
    @GetMapping("/questions/{id}/replies")
    public ResponseEntity<List<QuestionReply>> getReplies(@PathVariable Long id) {
        log.info("GET /api/faqs/questions/{}/replies", id);
        List<QuestionReply> replies = faqService.getReplies(id);
        return ResponseEntity.ok(replies);
    }

    /**
     * Add a reply to a question.
     */
    @PostMapping("/questions/{id}/reply")
    public ResponseEntity<QuestionReply> addReply(@PathVariable Long id, @RequestBody QuestionReply reply) {
        log.info("POST /api/faqs/questions/{}/reply, nickname={}", id, reply.getNickname());
        QuestionReply saved = faqService.addReply(id, reply);
        return ResponseEntity.ok(saved);
    }

    // ── Admin: FAQ Management ──

    /**
     * Admin: Create a new official FAQ.
     */
    @PostMapping("/admin")
    public ResponseEntity<Faq> createFaq(@RequestBody Faq faq) {
        log.info("POST /api/faqs/admin, question={}", faq.getQuestion());
        Faq created = faqService.createFaq(faq);
        return ResponseEntity.ok(created);
    }

    /**
     * Admin: Update an existing FAQ.
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<Faq> updateFaq(@PathVariable Long id, @RequestBody Faq update) {
        log.info("PUT /api/faqs/admin/{}", id);
        try {
            Faq updated = faqService.updateFaq(id, update);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("FAQ not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete an FAQ.
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        log.info("DELETE /api/faqs/admin/{}", id);
        try {
            faqService.deleteFaq(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("FAQ not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ── Admin: Question Moderation ──

    /**
     * Admin: Mark a user question as replied (change status).
     */
    @PutMapping("/admin/questions/{id}/reply")
    public ResponseEntity<Void> markReplied(@PathVariable Long id) {
        log.info("PUT /api/faqs/admin/questions/{}/reply", id);
        try {
            faqService.markReplied(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Question not found: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete a user question.
     */
    @DeleteMapping("/admin/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        log.info("DELETE /api/faqs/admin/questions/{}", id);
        try {
            faqService.deleteQuestion(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Question not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete a reply.
     */
    @DeleteMapping("/admin/replies/{id}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long id) {
        log.info("DELETE /api/faqs/admin/replies/{}", id);
        try {
            faqService.deleteReply(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Reply not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
