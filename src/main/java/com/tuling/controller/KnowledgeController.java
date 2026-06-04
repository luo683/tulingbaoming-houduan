package com.tuling.controller;

import com.tuling.entity.Faq;
import com.tuling.entity.KnowledgeDocument;
import com.tuling.service.FaqService;
import com.tuling.service.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 知识库管理控制器
 * 管理员 CRUD 知识文档 + RAG 检索回答
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private FaqService faqService;

    // ── 文档 CRUD（管理员）──

    @GetMapping("/admin/documents")
    public ResponseEntity<List<KnowledgeDocument>> getAllDocuments() {
        return ResponseEntity.ok(knowledgeService.getAllDocuments());
    }

    @GetMapping("/admin/documents/{id}")
    public ResponseEntity<KnowledgeDocument> getDocument(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(knowledgeService.getDocument(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/documents")
    public ResponseEntity<KnowledgeDocument> createDocument(@RequestBody KnowledgeDocument doc) {
        log.info("创建知识库文档: {}", doc.getTitle());
        return ResponseEntity.ok(knowledgeService.createDocument(doc));
    }

    @PutMapping("/admin/documents/{id}")
    public ResponseEntity<KnowledgeDocument> updateDocument(@PathVariable Long id, @RequestBody KnowledgeDocument doc) {
        try {
            return ResponseEntity.ok(knowledgeService.updateDocument(id, doc));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/admin/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            knowledgeService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── RAG 检索 ──

    /**
     * RAG 语义检索：输入问题，返回最相关的知识片段
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        log.info("RAG 检索: {}", q);

        List<KnowledgeService.ScoredChunk> vectors = knowledgeService.retrieveRelevantChunks(q);
        List<KnowledgeDocument> keywordMatch = knowledgeService.searchDocuments(q);

        Map<String, Object> result = new HashMap<>();
        result.put("query", q);

        // 向量检索结果
        List<Map<String, Object>> vectorResults = new ArrayList<>();
        for (KnowledgeService.ScoredChunk sc : vectors) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("text", sc.text);
            item.put("similarity", Math.round(sc.similarity * 10000.0) / 100.0); // percentage
            item.put("source", sc.source);
            item.put("documentId", sc.documentId);
            vectorResults.add(item);
        }
        result.put("vectorResults", vectorResults);

        // 关键词匹配结果
        List<Map<String, Object>> kwResults = new ArrayList<>();
        for (com.tuling.entity.KnowledgeDocument d : keywordMatch) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", d.getId());
            item.put("title", d.getTitle());
            item.put("category", d.getCategory() != null ? d.getCategory() : "");
            kwResults.add(item);
        }
        if (kwResults.size() > 5) {
            kwResults = kwResults.subList(0, 5);
        }
        result.put("keywordResults", kwResults);

        result.put("context", knowledgeService.retrieveContext(q));

        return ResponseEntity.ok(result);
    }

    /**
     * 使用知识库自动回答用户问题（管理员一键回复）
     */
    @PostMapping("/auto-reply/{questionId}")
    public ResponseEntity<Map<String, Object>> autoReply(@PathVariable Long questionId) {
        try {
            // 获取用户问题
            List<com.tuling.entity.UserQuestion> questions = faqService.getQuestions();
            com.tuling.entity.UserQuestion q = null;
            for (com.tuling.entity.UserQuestion uq : questions) {
                if (uq.getId().equals(questionId)) {
                    q = uq;
                    break;
                }
            }

            if (q == null) {
                return ResponseEntity.notFound().build();
            }

            String query = q.getTitle() + "\n" + (q.getContent() != null ? q.getContent() : "");
            String context = knowledgeService.retrieveContext(query);

            Map<String, Object> result = new HashMap<>();
            result.put("questionId", questionId);
            result.put("question", q.getTitle());
            result.put("context", context);
            result.put("hasAnswer", !context.isEmpty());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("自动回复失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 向量索引统计
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("documentCount", knowledgeService.getAllDocuments().size());
        return ResponseEntity.ok(stats);
    }

    /** 重新向量化全部文档（同步） */
    @PostMapping("/admin/reindex")
    public ResponseEntity<Map<String, String>> reindex() {
        knowledgeService.rebuildIndexFromDb();
        Map<String, String> result = new HashMap<>();
        result.put("status", "done");
        return ResponseEntity.ok(result);
    }
}
