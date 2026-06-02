package com.tuling.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG（检索增强生成）服务
 * 使用本地 Ollama bge-m3 向量模型进行文档向量化
 * Redis 存储向量数据实现持久化
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static final String REDIS_VECTOR_PREFIX = "rag:vector:";
    private static final String REDIS_TEXT_PREFIX = "rag:text:";
    private static final String REDIS_INDEX_KEY = "rag:index";

    @Value("${ai.rag.knowledge-path}")
    private String knowledgePath;

    @Value("${ai.rag.chunk-size}")
    private int chunkSize;

    @Value("${ai.rag.chunk-overlap}")
    private int chunkOverlap;

    @Value("${ai.rag.max-results}")
    private int maxResults;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 存储文档块及其向量的内存缓存
     */
    private final List<DocumentChunk> documentChunks = new ArrayList<>();

    /**
     * 应用启动时加载知识库并向量化（使用本地 Ollama bge-m3）
     */
    @PostConstruct
    public void init() {
        try {
            log.info("开始加载知识库: {}", knowledgePath);
            String content = loadKnowledgeFile();
            if (content == null || content.trim().isEmpty()) {
                log.warn("知识库文件为空，跳过 RAG 初始化");
                return;
            }

            // 分块
            List<String> chunks = splitIntoChunks(content);
            log.info("知识库分块完成，共 {} 个文本块，开始向量化（本地 Ollama bge-m3）...", chunks.size());

            // 向量化并存储
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                try {
                    Embedding embedding = embeddingModel.embed(chunk).content();
                    float[] vector = embedding.vector();

                    // 存储到内存
                    documentChunks.add(new DocumentChunk(chunk, vector, i));

                    // 同时存储到 Redis（持久化）
                    storeToRedis(i, chunk, vector);
                } catch (Exception e) {
                    log.warn("文本块 {} 向量化失败: {}", i, e.getMessage());
                }
            }

            log.info("RAG 知识库初始化完成，成功向量化 {} 个文本块", documentChunks.size());
        } catch (Exception e) {
            log.error("RAG 知识库初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检索与用户问题最相关的文档上下文
     *
     * @param query 用户查询
     * @return 相关文档内容拼接后返回
     */
    public String retrieveRelevantContext(String query) {
        if (documentChunks.isEmpty()) {
            return "";
        }

        try {
            // 将用户查询向量化
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            float[] queryVector = queryEmbedding.vector();

            // 计算与所有文档块的余弦相似度
            List<ScoredChunk> scoredChunks = new ArrayList<>();
            for (DocumentChunk chunk : documentChunks) {
                double similarity = cosineSimilarity(queryVector, chunk.vector);
                scoredChunks.add(new ScoredChunk(chunk.text, similarity));
            }

            // 按相似度降序排序，取 top-k
            scoredChunks.sort((a, b) -> Double.compare(b.score, a.score));
            List<ScoredChunk> topResults = scoredChunks.subList(0, Math.min(maxResults, scoredChunks.size()));

            // 过滤掉相似度过低的结果（阈值 0.3）
            String context = topResults.stream()
                    .filter(sc -> sc.score > 0.3)
                    .map(sc -> sc.text)
                    .collect(Collectors.joining("\n\n---\n\n"));

            log.debug("RAG 检索完成，匹配 {} 个文档块，最高相似度: {}",
                    topResults.size(),
                    topResults.isEmpty() ? 0 : String.format("%.4f", topResults.get(0).score));

            return context;
        } catch (Exception e) {
            log.error("RAG 检索失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 加载知识库文件
     */
    private String loadKnowledgeFile() {
        try {
            ClassPathResource resource = new ClassPathResource(knowledgePath);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            log.error("知识库文件加载失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 将文本按指定大小分块（带重叠）
     */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                String overlap = currentChunk.substring(
                        Math.max(0, currentChunk.length() - chunkOverlap));
                currentChunk = new StringBuilder(overlap);
            }
            currentChunk.append(paragraph).append("\n\n");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 将向量存储到 Redis
     */
    private void storeToRedis(int index, String text, float[] vector) {
        try {
            String vectorStr = Arrays.toString(vector);
            stringRedisTemplate.opsForValue().set(REDIS_VECTOR_PREFIX + index, vectorStr);
            stringRedisTemplate.opsForValue().set(REDIS_TEXT_PREFIX + index, text);
            stringRedisTemplate.opsForSet().add(REDIS_INDEX_KEY, String.valueOf(index));
        } catch (Exception e) {
            log.warn("向量存储到 Redis 失败: {}", e.getMessage());
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * 文档块内部类
     */
    private static class DocumentChunk {
        final String text;
        final float[] vector;
        final int index;

        DocumentChunk(String text, float[] vector, int index) {
            this.text = text;
            this.vector = vector;
            this.index = index;
        }
    }

    /**
     * 带分数的文档块
     */
    private static class ScoredChunk {
        final String text;
        final double score;

        ScoredChunk(String text, double score) {
            this.text = text;
            this.score = score;
        }
    }
}
