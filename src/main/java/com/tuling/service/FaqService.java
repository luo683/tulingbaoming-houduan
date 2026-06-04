package com.tuling.service;

import com.tuling.entity.Faq;
import com.tuling.entity.QuestionReply;
import com.tuling.entity.UserQuestion;
import com.tuling.repository.FaqRepository;
import com.tuling.repository.QuestionReplyRepository;
import com.tuling.repository.UserQuestionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FaqService {

    private static final Logger log = LoggerFactory.getLogger(FaqService.class);

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private UserQuestionRepository userQuestionRepository;

    @Autowired
    private QuestionReplyRepository questionReplyRepository;

    // ── Official FAQs ──

    /**
     * Get all FAQs, ordered by sort order ascending.
     */
    public List<Faq> getAllFaqs() {
        log.debug("Fetching all FAQs");
        return faqRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Create a new FAQ.
     */
    @Transactional
    public Faq createFaq(Faq faq) {
        log.info("Creating new FAQ: {}", faq.getQuestion());
        Faq saved = faqRepository.save(faq);
        log.info("FAQ created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing FAQ. Only non-null fields from the update are applied.
     */
    @Transactional
    public Faq updateFaq(Long id, Faq update) {
        log.info("Updating FAQ id: {}", id);
        Faq existing = faqRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("FAQ not found with id: " + id));

        if (update.getQuestion() != null) {
            existing.setQuestion(update.getQuestion());
        }
        if (update.getAnswer() != null) {
            existing.setAnswer(update.getAnswer());
        }
        if (update.getAuthor() != null) {
            existing.setAuthor(update.getAuthor());
        }
        // sortOrder is a primitive int, always set it (0 is a valid value)
        existing.setSortOrder(update.getSortOrder());

        Faq saved = faqRepository.save(existing);
        log.info("FAQ updated: {}", saved.getId());
        return saved;
    }

    /**
     * Delete a FAQ by id.
     */
    @Transactional
    public void deleteFaq(Long id) {
        log.info("Deleting FAQ id: {}", id);
        if (!faqRepository.existsById(id)) {
            throw new NoSuchElementException("FAQ not found with id: " + id);
        }
        faqRepository.deleteById(id);
        log.info("FAQ deleted: {}", id);
    }

    // ── User Questions ──

    /**
     * Get all user questions (non-deleted), newest first.
     */
    public List<UserQuestion> getQuestions() {
        log.debug("Fetching all user questions");
        // Get both pending and replied questions
        List<UserQuestion> pending = userQuestionRepository.findByStatusOrderByCreatedAtDesc("pending");
        List<UserQuestion> replied = userQuestionRepository.findByStatusOrderByCreatedAtDesc("replied");
        pending.addAll(replied);
        return pending;
    }

    /**
     * Submit a new user question. Sets status to pending by default.
     */
    @Transactional
    public UserQuestion askQuestion(UserQuestion question) {
        log.info("New user question from: {}", question.getNickname());
        question.setStatus("pending");
        UserQuestion saved = userQuestionRepository.save(question);
        log.info("User question saved with id: {}", saved.getId());
        return saved;
    }

    /**
     * Mark a user question as replied.
     */
    @Transactional
    public void markReplied(Long id) {
        log.info("Marking question {} as replied", id);
        UserQuestion question = userQuestionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User question not found with id: " + id));
        question.setStatus("replied");
        userQuestionRepository.save(question);
        log.info("Question {} marked as replied", id);
    }

    /**
     * Delete a user question by id.
     */
    @Transactional
    public void deleteQuestion(Long id) {
        log.info("Deleting user question id: {}", id);
        if (!userQuestionRepository.existsById(id)) {
            throw new NoSuchElementException("User question not found with id: " + id);
        }
        userQuestionRepository.deleteById(id);
        log.info("User question deleted: {}", id);
    }

    // ── Replies ──

    /**
     * Get all replies for a question, oldest first.
     */
    public List<QuestionReply> getReplies(Long questionId) {
        log.debug("Fetching replies for question id: {}", questionId);
        return questionReplyRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
    }

    /**
     * Add a reply to a user question.
     */
    @Transactional
    public QuestionReply addReply(Long questionId, QuestionReply reply) {
        log.info("Adding reply to question id: {}", questionId);
        reply.setQuestionId(questionId);
        QuestionReply saved = questionReplyRepository.save(reply);
        log.info("Reply added with id: {}", saved.getId());
        return saved;
    }

    /**
     * Delete a reply by id.
     */
    @Transactional
    public void deleteReply(Long id) {
        log.info("Deleting reply id: {}", id);
        if (!questionReplyRepository.existsById(id)) {
            throw new NoSuchElementException("Reply not found with id: " + id);
        }
        questionReplyRepository.deleteById(id);
        log.info("Reply deleted: {}", id);
    }
}
