package com.tuling.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内本地缓存 —— 用于替代 Redis。
 *
 * <p>背景：本项目原本把「RAG 向量持久化 / 会话 TTL / 接口限流计数」放在 Redis 里，
 * 但开发机上并没有跑 Redis，导致每次启动都刷一堆
 * {@code Unable to connect to localhost:6379} 的连接异常（虽然服务会降级放行，但日志很脏，
 * 而且限流事实上完全没生效）。
 *
 * <p>这里提供一个带 TTL 的本地实现：
 * <ul>
 *   <li>开发 / 单机部署：开箱可用，不需要额外装服务；</li>
 *   <li>需要多实例共享状态时：把 {@code app.cache.enabled} 置为 false，
 *       再把 Redis 依赖接回来即可，调用方接口保持不变。</li>
 * </ul>
 *
 * <p>线程安全，TTL 采用「读取时惰性过期 + 写入时顺带清扫」策略，不额外起定时线程。
 */
@Component
public class LocalCache {

    private static final Logger log = LoggerFactory.getLogger(LocalCache.class);

    /** 每次写入时最多顺带清扫的过期条目数，避免单次写入开销不可控 */
    private static final int SWEEP_LIMIT = 64;

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 写入并设置过期时间（秒）。ttlSeconds <= 0 表示永不过期。 */
    public void put(String key, String value, long ttlSeconds) {
        long expireAt = ttlSeconds > 0
                ? System.currentTimeMillis() + ttlSeconds * 1000L
                : Long.MAX_VALUE;
        store.put(key, new Entry(value, expireAt));
        sweepIfNeeded();
    }

    /** 读取；已过期则返回 null 并顺带移除。 */
    public String get(String key) {
        Entry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.remove(key, entry);
            return null;
        }
        return entry.value;
    }

    /**
     * 自增计数；键不存在或已过期时从 1 开始。
     *
     * @param key        计数键
     * @param ttlSeconds 首次创建时设置的过期时间（秒），用于实现固定窗口
     * @return 自增后的值
     */
    public long increment(String key, long ttlSeconds) {
        long now = System.currentTimeMillis();
        Entry updated = store.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpiredAt(now)) {
                long expireAt = ttlSeconds > 0 ? now + ttlSeconds * 1000L : Long.MAX_VALUE;
                return new Entry("1", expireAt);
            }
            long next = parseLong(existing.value) + 1;
            return new Entry(String.valueOf(next), existing.expireAt);
        });
        sweepIfNeeded();
        return parseLong(updated.value);
    }

    /** 删除指定键。 */
    public void remove(String key) {
        store.remove(key);
    }

    /** 当前存活条目数（含尚未被惰性清扫的过期条目之外的近似值，仅用于日志/诊断）。 */
    public int size() {
        return store.size();
    }

    private void sweepIfNeeded() {
        int removed = 0;
        Iterator<Map.Entry<String, Entry>> it = store.entrySet().iterator();
        while (it.hasNext() && removed < SWEEP_LIMIT) {
            Map.Entry<String, Entry> e = it.next();
            if (e.getValue().isExpired()) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("本地缓存清扫了 {} 个过期条目，剩余 {}", removed, store.size());
        }
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** 缓存条目：值 + 绝对过期时间戳 */
    private static class Entry {
        final String value;
        final long expireAt;

        Entry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return isExpiredAt(System.currentTimeMillis());
        }

        boolean isExpiredAt(long now) {
            return expireAt != Long.MAX_VALUE && expireAt <= now;
        }
    }
}
