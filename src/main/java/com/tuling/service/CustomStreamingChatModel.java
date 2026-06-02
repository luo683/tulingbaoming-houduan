package com.tuling.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 自定义流式聊天模型 —— 直接对接硅基流动 / OpenAI 兼容接口
 * 绕开 langchain4j 内部的 jtokkit token 计数和响应解析问题
 * 使用 JDK 原生 HttpURLConnection 实现 SSE 流式读取
 */
public class CustomStreamingChatModel implements StreamingChatLanguageModel {

    private static final Logger log = LoggerFactory.getLogger(CustomStreamingChatModel.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final String apiKey;
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;

    public CustomStreamingChatModel(String baseUrl, String apiKey, String modelName,
                                    Double temperature, Integer maxTokens) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
        try {
            // 构建请求 JSON
            String requestBody = buildRequestBody(messages);

            // 建立 HTTP 连接
            String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);

            // 发送请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 检查响应码
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorBody = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorBody.append(line);
                }
                errorReader.close();
                String errorMsg = "API 请求失败，HTTP " + responseCode + ": " + errorBody.toString();
                log.error(errorMsg);
                handler.onError(new RuntimeException(errorMsg));
                return;
            }

            // 读取 SSE 流式响应
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder fullContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // SSE 格式：data: {...}
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();

                    // 结束标记
                    if ("[DONE]".equals(data)) {
                        break;
                    }

                    try {
                        // 解析 JSON
                        JsonNode jsonNode = objectMapper.readTree(data);
                        JsonNode choices = jsonNode.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").asText("");
                                if (!content.isEmpty()) {
                                    fullContent.append(content);
                                    // 逐字推送给 handler
                                    handler.onNext(content);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("解析 SSE 数据行异常（可忽略）: {}", e.getMessage());
                    }
                }
            }

            reader.close();
            conn.disconnect();

            // 完成回调
            AiMessage aiMessage = AiMessage.from(fullContent.toString());
            handler.onComplete(Response.from(aiMessage));

        } catch (Exception e) {
            log.error("流式聊天请求异常: {}", e.getMessage(), e);
            handler.onError(e);
        }
    }

    /**
     * 构建 OpenAI 兼容格式的请求体
     */
    private String buildRequestBody(List<ChatMessage> messages) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", modelName);
        root.put("stream", true);
        root.put("temperature", temperature);
        root.put("max_tokens", maxTokens);

        ArrayNode messagesArray = root.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = objectMapper.createObjectNode();
            if (msg instanceof SystemMessage) {
                msgNode.put("role", "system");
                msgNode.put("content", ((SystemMessage) msg).text());
            } else if (msg instanceof UserMessage) {
                msgNode.put("role", "user");
                msgNode.put("content", ((UserMessage) msg).text());
            } else if (msg instanceof AiMessage) {
                msgNode.put("role", "assistant");
                msgNode.put("content", ((AiMessage) msg).text());
            }
            messagesArray.add(msgNode);
        }

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Builder 模式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private Double temperature = 0.7;
        private Integer maxTokens = 2048;

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder temperature(Double temperature) { this.temperature = temperature; return this; }
        public Builder maxTokens(Integer maxTokens) { this.maxTokens = maxTokens; return this; }

        public CustomStreamingChatModel build() {
            return new CustomStreamingChatModel(baseUrl, apiKey, modelName, temperature, maxTokens);
        }
    }
}
