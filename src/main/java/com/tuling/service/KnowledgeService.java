package com.tuling.service;

import com.tuling.entity.KnowledgeDocument;
import com.tuling.repository.KnowledgeDocumentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 知识库服务 — 文档 CRUD + RAG 向量检索
 * 使用 Ollama Embedding 模型将文档向量化，支持语义检索
 */
@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    @Autowired
    private KnowledgeDocumentRepository docRepository;

    @Autowired
    private EmbeddingModel embeddingModel;

    /** 内存向量索引：docId → List<Chunk> */
    private final ConcurrentHashMap<Long, List<ChunkVector>> vectorIndex = new ConcurrentHashMap<>();

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;
    private static final int MAX_RESULTS = 5;
    private static final double MIN_SIMILARITY = 0.15;

    /**
     * 启动时从数据库加载所有文档并向量化
     */
    @PostConstruct
    public void init() {
        new Thread(this::rebuildIndexFromDb, "knowledge-init").start();
    }

    /** 从数据库重新构建向量索引（同步执行） */
    public void rebuildIndexFromDb() {
        try {
            vectorIndex.clear();
            List<KnowledgeDocument> docs = docRepository.findAll();
            log.info("开始向量化 {} 篇知识库文档...", docs.size());
            int success = 0;
            for (KnowledgeDocument doc : docs) {
                try {
                    indexDocument(doc);
                    success++;
                } catch (Exception e) {
                    log.warn("文档 [{}] 向量化失败: {}", doc.getTitle(), e.getMessage());
                }
            }
            log.info("知识库向量化完成: {}/{} 篇", success, docs.size());
        } catch (Exception e) {
            log.error("知识库初始化失败: {}", e.getMessage());
        }
    }

    // ── CRUD ──

    public List<KnowledgeDocument> getAllDocuments() {
        return docRepository.findAllByOrderByUpdatedAtDesc();
    }

    public KnowledgeDocument getDocument(Long id) {
        return docRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("文档不存在: " + id));
    }

    @Transactional
    public KnowledgeDocument createDocument(KnowledgeDocument doc) {
        KnowledgeDocument saved = docRepository.save(doc);
        // 同步向量化
        try { indexDocument(saved); } catch (Exception e) { log.warn("向量化失败: {}", e.getMessage()); }
        return saved;
    }

    @Transactional
    public KnowledgeDocument updateDocument(Long id, KnowledgeDocument update) {
        KnowledgeDocument doc = docRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("文档不存在: " + id));
        doc.setTitle(update.getTitle());
        doc.setContent(update.getContent());
        doc.setCategory(update.getCategory());
        doc.setTags(update.getTags());
        KnowledgeDocument saved = docRepository.save(doc);
        // 重新向量化
        vectorIndex.remove(id);
        new Thread(() -> { try { indexDocument(saved); } catch (Exception e) { log.warn("向量化失败: {}", e.getMessage()); } }).start();
        return saved;
    }

    @Transactional
    public void deleteDocument(Long id) {
        docRepository.deleteById(id);
        vectorIndex.remove(id);
    }

    // ── 向量化 ──

    /**
     * 对单个文档进行分块 + 向量化，存入内存索引
     */
    private void indexDocument(KnowledgeDocument doc) {
        String text = doc.getTitle() + "\n" + doc.getContent();
        List<String> chunks = splitIntoChunks(text);

        List<ChunkVector> vectors = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            try {
                Embedding emb = embeddingModel.embed(chunks.get(i)).content();
                vectors.add(new ChunkVector(chunks.get(i), emb.vector(), i));
            } catch (Exception e) {
                log.warn("文档 [{}] 分块 {} 向量化失败: {}", doc.getTitle(), i, e.getMessage());
            }
        }
        vectorIndex.put(doc.getId(), vectors);
        log.debug("文档 [{}] 向量化完成: {} 个分块", doc.getTitle(), vectors.size());
    }

    // ── RAG 检索 ──

    /**
     * 检索与查询最相关的知识片段
     * @param query 用户查询
     * @return 相关文本片段列表
     */
    public List<ScoredChunk> retrieveRelevantChunks(String query) {
        if (vectorIndex.isEmpty()) {
            log.warn("向量索引为空，跳过 RAG 检索");
            return Collections.emptyList();
        }

        try {
            // 查询向量化
            Embedding queryEmb = embeddingModel.embed(query).content();
            float[] queryVec = queryEmb.vector();

            // 对所有文档的所有分块计算余弦相似度
            List<ScoredChunk> allChunks = new ArrayList<>();
            for (Map.Entry<Long, List<ChunkVector>> entry : vectorIndex.entrySet()) {
                Long docId = entry.getKey();
                for (ChunkVector cv : entry.getValue()) {
                    double sim = cosineSimilarity(queryVec, cv.vector);
                    if (sim >= MIN_SIMILARITY) {
                        // 获取文档标题作为来源标识
                        String source = docRepository.findById(docId)
                                .map(KnowledgeDocument::getTitle)
                                .orElse("文档#" + docId);
                        allChunks.add(new ScoredChunk(cv.text, sim, source, docId));
                    }
                }
            }

            // 按相似度降序，取 top-k
            allChunks.sort((a, b) -> Double.compare(b.similarity, a.similarity));
            return allChunks.subList(0, Math.min(MAX_RESULTS, allChunks.size()));

        } catch (Exception e) {
            log.error("RAG 检索失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 完整 RAG 回答：检索 + 拼接上下文
     */
    public String retrieveContext(String query) {
        List<ScoredChunk> chunks = retrieveRelevantChunks(query);
        if (chunks.isEmpty()) return "";

        return chunks.stream()
                .map(c -> "【来源：" + c.source + "】\n" + c.text)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    /**
     * 关键词搜索（MySQL 全文匹配，补充向量检索）
     */
    public List<KnowledgeDocument> searchDocuments(String keyword) {
        return docRepository.searchByKeyword(keyword);
    }

    // ── 工具方法 ──

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n");
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            if (current.length() + para.length() > CHUNK_SIZE && current.length() > 0) {
                chunks.add(current.toString().trim());
                String overlap = current.substring(Math.max(0, current.length() - CHUNK_OVERLAP));
                current = new StringBuilder(overlap);
            }
            current.append(para).append("\n\n");
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0.0 : dot / denom;
    }

    // ── 内部类 ──

    private static class ChunkVector {
        final String text;
        final float[] vector;
        final int index;

        ChunkVector(String text, float[] vector, int index) {
            this.text = text;
            this.vector = vector;
            this.index = index;
        }
    }

    public static class ScoredChunk {
        public final String text;
        public final double similarity;
        public final String source;
        public final Long documentId;

        public ScoredChunk(String text, double similarity, String source, Long documentId) {
            this.text = text;
            this.similarity = similarity;
            this.source = source;
            this.documentId = documentId;
        }
    }
}
