package com.tuling.controller;

import com.tuling.model.ChatRequest;
import com.tuling.service.ChatService;
import com.tuling.service.RateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * AI 聊天控制器
 * 提供 SSE 流式接口，支持打字机效果的 AI 问答
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /**
     * SSE 连接超时时间：5 分钟
     */
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

    @Autowired
    private ChatService chatService;

    @Autowired
    private RateLimitService rateLimitService;

    /**
     * 流式聊天接口（SSE）
     * 前端通过 POST 请求发送 prompt，后端通过 SSE 逐字返回 AI 回复
     *
     * @param request     聊天请求体，包含 message 和 sessionId
     * @param httpRequest HTTP 请求（用于获取客户端 IP）
     * @return SseEmitter 流式响应
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        // 获取客户端 IP
        String clientIp = getClientIp(httpRequest);

        // 限流检查
        if (!rateLimitService.isAllowed(clientIp)) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event()
                        .data("请求过于频繁，请稍后再试。（每分钟限制 10 次请求）")
                        .name("error"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 如果没有 sessionId，自动生成一个
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // 参数校验
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event()
                        .data("消息内容不能为空")
                        .name("error"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        log.info("收到聊天请求 - 会话ID: {}, IP: {}, 消息: {}",
                sessionId, clientIp, request.getMessage().substring(0, Math.min(50, request.getMessage().length())));

        // 创建 SSE 发射器
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 设置超时和错误回调
        final String finalSessionId = sessionId;
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时，会话ID: {}", finalSessionId);
            emitter.complete();
        });
        emitter.onError(throwable -> {
            log.error("SSE 连接异常，会话ID: {}, 错误: {}", finalSessionId, throwable.getMessage());
        });
        emitter.onCompletion(() -> {
            log.debug("SSE 连接关闭，会话ID: {}", finalSessionId);
        });

        // 异步执行流式聊天
        String finalSessionId2 = sessionId;
        new Thread(() -> chatService.streamChat(
                finalSessionId2,
                request.getMessage(),
                request.isEnableRag(),
                emitter
        )).start();

        return emitter;
    }

    /**
     * 清除会话记忆
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @PostMapping("/clear/{sessionId}")
    public ResponseEntity<Map<String, String>> clearMemory(@PathVariable String sessionId) {
        chatService.clearMemory(sessionId);
        return ResponseEntity.ok(Collections.singletonMap("message", "会话记忆已清除"));
    }

    /**
     * 获取限流状态
     *
     * @param httpRequest HTTP 请求
     * @return 剩余请求数
     */
    @GetMapping("/rate-limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        int remaining = rateLimitService.getRemainingRequests(clientIp);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("remaining", remaining);
        result.put("limit", 10);
        return ResponseEntity.ok(result);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    /**
     * 获取客户端真实 IP 地址
     * 支持经过 Nginx 等代理转发的场景
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
