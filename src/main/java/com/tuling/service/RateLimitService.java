package com.tuling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 限流服务
 * 基于 Redis 实现滑动窗口限流，防止接口被滥用
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Value("${ai.rate-limit.max-requests-per-minute}")
    private int maxRequestsPerMinute;

    @Value("${ai.rate-limit.window-seconds}")
    private int windowSeconds;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 检查指定客户端是否被限流
     *
     * @param clientId 客户端标识（通常为 IP 地址）
     * @return true = 请求被允许，false = 已达到限流阈值
     */
    public boolean isAllowed(String clientId) {
        String key = RATE_LIMIT_PREFIX + clientId;
        try {
            // 获取当前计数
            String countStr = stringRedisTemplate.opsForValue().get(key);
            int currentCount = countStr == null ? 0 : Integer.parseInt(countStr);

            if (currentCount >= maxRequestsPerMinute) {
                log.warn("客户端 {} 触发限流，当前请求数: {}/{}", clientId, currentCount, maxRequestsPerMinute);
                return false;
            }

            // 增加计数
            Long newCount = stringRedisTemplate.opsForValue().increment(key);
            if (newCount != null && newCount == 1) {
                // 第一次请求，设置过期时间
                stringRedisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }

            return true;
        } catch (Exception e) {
            // Redis 不可用时，放行请求（降级策略）
            log.error("限流服务 Redis 异常，降级放行: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 获取客户端剩余可用请求数
     *
     * @param clientId 客户端标识
     * @return 剩余请求数
     */
    public int getRemainingRequests(String clientId) {
        String key = RATE_LIMIT_PREFIX + clientId;
        try {
            String countStr = stringRedisTemplate.opsForValue().get(key);
            int currentCount = countStr == null ? 0 : Integer.parseInt(countStr);
            return Math.max(0, maxRequestsPerMinute - currentCount);
        } catch (Exception e) {
            return maxRequestsPerMinute;
        }
    }
}
