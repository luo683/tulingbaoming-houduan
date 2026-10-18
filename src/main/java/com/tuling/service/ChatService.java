package com.tuling.service;

import com.tuling.config.LocalCache;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 聊天服务
 * 支持流式响应（SSE 打字机效果）和上下文记忆
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    /**
     * 系统提示词：定义 AI 的角色和行为
     */
    private static final String SYSTEM_PROMPT = "你是图灵学社的 AI 助手，专门为计算机专业学生提供技术问答服务。"
            + "你的回答应该专业、简洁、有帮助。"
            + "如果提供了参考资料，请基于参考资料回答问题，并在回答中适当引用。"
            + "如果不确定答案，请诚实地说明。";

    /**
     * 会话记忆存储：sessionId -> 消息历史列表
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final ConcurrentHashMap<String, List<ChatMessage>> sessionMemory = new ConcurrentHashMap<>();

    /**
     * 每个会话最多保留的历史消息数量（滑动窗口）
     */
    private static final int MAX_MEMORY_SIZE = 20;

    /**
     * 会话过期时间（进程内缓存中的 TTL，分钟）
     */
    private static final long SESSION_TTL_MINUTES = 30;

    @Autowired
    private StreamingChatLanguageModel streamingChatModel;

    @Autowired
    private RagService ragService;

    @Autowired
    private LocalCache localCache;

    /**
     * 处理流式聊天请求
     *
     * @param sessionId  会话ID
     * @param userInput  用户输入
     * @param enableRag  是否启用 RAG 知识库检索
     * @param emitter    SSE 发射器，用于向前端推送数据
     */
    public void streamChat(String sessionId, String userInput, boolean enableRag, SseEmitter emitter) {
        try {
            // 1. 构建消息列表
            List<ChatMessage> messages = buildMessages(sessionId, userInput, enableRag);

            // 2. 记录用户消息到会话历史
            addToMemory(sessionId, UserMessage.from(userInput));

            // 3. 用于收集完整的 AI 回复
            StringBuilder fullResponse = new StringBuilder();

            // 4. 调用大模型进行流式生成
            streamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    // 每收到一个 token，通过 SSE 推送给前端
                    try {
                        emitter.send(SseEmitter.event()
                                .data(token)
                                .name("message"));
                        fullResponse.append(token);
                    } catch (Exception e) {
                        log.error("SSE 发送 token 失败: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        // 将完整的 AI 回复存入会话历史
                        addToMemory(sessionId, AiMessage.from(fullResponse.toString()));
                        // 刷新会话过期时间
                        refreshSessionTTL(sessionId);
                        // 发送完成事件
                        emitter.send(SseEmitter.event()
                                .data("[DONE]")
                                .name("done"));
                        emitter.complete();
                        log.info("会话 {} 流式响应完成，回复长度: {}", sessionId, fullResponse.length());
                    } catch (Exception e) {
                        log.error("SSE 完成事件发送失败: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("大模型调用异常，会话ID: {}, 错误: {}", sessionId, error.getMessage());
                    try {
                        emitter.send(SseEmitter.event()
                                .data("抱歉，AI 服务暂时不可用，请稍后重试。")
                                .name("error"));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(error);
                    }
                }
            });

        } catch (Exception e) {
            log.error("流式聊天处理异常，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event()
                        .data("系统异常，请稍后重试。")
                        .name("error"));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(e);
            }
        }
    }

    /**
     * 构建发送给大模型的消息列表
     * 包含：系统提示词 + RAG 参考资料 + 历史上下文 + 当前用户消息
     */
    private List<ChatMessage> buildMessages(String sessionId, String userInput, boolean enableRag) {
        List<ChatMessage> messages = new ArrayList<>();

        // 1. 添加系统提示词
        String systemPrompt = SYSTEM_PROMPT;

        // 2. 如果启用 RAG，检索相关知识并加入系统提示
        if (enableRag) {
            String context = ragService.retrieveRelevantContext(userInput);
            if (context != null && !context.isEmpty()) {
                systemPrompt += "\n\n以下是与用户问题相关的参考资料：\n" + context;
            }
        }
        messages.add(SystemMessage.from(systemPrompt));

        // 3. 添加历史对话上下文（滑动窗口）
        List<ChatMessage> history = sessionMemory.getOrDefault(sessionId, new ArrayList<>());
        messages.addAll(history);

        // 4. 添加当前用户消息
        messages.add(UserMessage.from(userInput));

        return messages;
    }

    /**
     * 将消息添加到会话记忆中（滑动窗口机制）
     */
    private void addToMemory(String sessionId, ChatMessage message) {
        sessionMemory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        List<ChatMessage> history = sessionMemory.get(sessionId);
        history.add(message);

        // 滑动窗口：保留最近的 N 条消息
        while (history.size() > MAX_MEMORY_SIZE) {
            history.remove(0);
        }
    }

    /**
     * 刷新会话 TTL（进程内缓存管理会话过期）
     */
    private void refreshSessionTTL(String sessionId) {
        try {
            localCache.put("chat:session:" + sessionId, "active", SESSION_TTL_MINUTES * 60);
        } catch (Exception e) {
            log.warn("会话 TTL 刷新失败: {}", e.getMessage());
        }
    }

    /**
     * 清除指定会话的记忆
     */
    public void clearMemory(String sessionId) {
        sessionMemory.remove(sessionId);
        try {
            localCache.remove("chat:session:" + sessionId);
        } catch (Exception e) {
            log.warn("会话缓存删除失败: {}", e.getMessage());
        }
        log.info("会话 {} 记忆已清除", sessionId);
    }
}
