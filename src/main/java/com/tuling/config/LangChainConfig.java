package com.tuling.config;

import com.tuling.service.CustomStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 大模型配置类
 * 聊天模型：自定义实现，直接对接硅基流动 API（绕开 jtokkit 和 token 计数问题）
 * 向量模型：本地 Ollama bge-m3
 */
@Configuration
public class LangChainConfig {

    @Value("${ai.model.base-url}")
    private String baseUrl;

    @Value("${ai.model.api-key}")
    private String apiKey;

    @Value("${ai.model.model-name}")
    private String modelName;

    @Value("${ai.model.temperature}")
    private Double temperature;

    @Value("${ai.model.max-tokens}")
    private Integer maxTokens;

    @Value("${ai.embedding.base-url}")
    private String embeddingBaseUrl;

    @Value("${ai.embedding.api-key}")
    private String embeddingApiKey;

    @Value("${ai.embedding.model-name}")
    private String embeddingModelName;

    /**
     * 流式聊天模型 Bean
     * 使用自定义实现，直接 HTTP 调用硅基流动 API
     * 完全绕开 langchain4j 内部的 jtokkit/token 计数/响应解析问题
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return CustomStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    /**
     * Embedding 向量模型 Bean（用于 RAG 知识库向量化）
     * 使用本地 Ollama 部署的 bge-m3 模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingBaseUrl)
                .apiKey(embeddingApiKey)
                .modelName(embeddingModelName)
                .build();
    }
}
