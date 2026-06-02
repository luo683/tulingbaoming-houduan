package com.tuling.model;

/**
 * 聊天请求体
 */
public class ChatRequest {

    /**
     * 用户输入的消息内容
     */
    private String message;

    /**
     * 会话ID，用于维持上下文记忆
     */
    private String sessionId;

    /**
     * 是否启用 RAG 知识库检索
     */
    private boolean enableRag = true;

    public ChatRequest() {
    }

    public ChatRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isEnableRag() {
        return enableRag;
    }

    public void setEnableRag(boolean enableRag) {
        this.enableRag = enableRag;
    }
}
