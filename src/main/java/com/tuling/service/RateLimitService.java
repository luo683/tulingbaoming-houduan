package com.tuling.service;

import com.tuling.config.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 限流服务
 * 基于进程内缓存实现固定窗口限流，防止接口被滥用。
 *
 * <p>说明：原实现依赖 Redis，但开发机没有 Redis，导致限流实际上一直是「降级放行」的
 * 空壳，同时还刷连接异常日志。现改为 {@link LocalCache} 内存计数 ——
 * 功能真正生效，且不依赖外部服务。
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
    private LocalCache localCache;

    /**
     * 检查指定客户端是否被限流
     *
     * @param clientId 客户端标识（通常为 IP 地址）
     * @return true = 请求被允许，false = 已达到限流阈值
     */
    public boolean isAllowed(String clientId) {
        String key = RATE_LIMIT_PREFIX + clientId;
        try {
            long currentCount = localCache.increment(key, windowSeconds);

            if (currentCount > maxRequestsPerMinute) {
                log.warn("客户端 {} 触发限流，当前请求数: {}/{}", clientId, currentCount, maxRequestsPerMinute);
                return false;
            }

            return true;
        } catch (Exception e) {
            // 限流组件自身异常时放行，避免把业务一起拖垮
            log.error("限流计数异常，降级放行: {}", e.getMessage());
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
            String countStr = localCache.get(key);
            int currentCount = countStr == null ? 0 : Integer.parseInt(countStr);
            return Math.max(0, maxRequestsPerMinute - currentCount);
        } catch (Exception e) {
            return maxRequestsPerMinute;
        }
    }
}
