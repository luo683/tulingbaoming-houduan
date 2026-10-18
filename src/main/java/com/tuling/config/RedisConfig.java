package com.tuling.config;

import org.springframework.context.annotation.Configuration;

/**
 * Redis 配置 —— 已停用。
 *
 * <p>项目原本用 Redis 存 RAG 向量、会话 TTL 和限流计数，但开发机没有 Redis，
 * 导致启动即刷连接异常、限流也从未真正生效。
 * 现统一改用 {@link LocalCache}（进程内带 TTL 的缓存）。
 *
 * <p>使用 {@code @Configuration} 而非 {@code @ConfigurationProperties} 无副作用：
 * 本类不再注册任何 Bean，纯粹保留占位说明，方便日后需要时接回 Redis。
 * 若确定不再需要，可直接删除本文件。
 */
@Configuration
public class RedisConfig {
    // 原 RedisTemplate / StringRedisTemplate Bean 定义已移除。
    // 接回方式：恢复 spring-boot-starter-data-redis 依赖 + 恢复下面两个 Bean，
    // 并把 LocalCache 的注入点替换回 StringRedisTemplate。
}
