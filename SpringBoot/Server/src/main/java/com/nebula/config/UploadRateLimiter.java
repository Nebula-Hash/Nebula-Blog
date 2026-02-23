package com.nebula.config;

import com.nebula.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 上传接口简易限流器
 * 基于固定窗口（每分钟）+ IP 维度，防止恶意频繁上传
 *
 * @author Nebula-Hash
 * @date 2026/2/23
 */
@Slf4j
@Component
public class UploadRateLimiter {

    /**
     * 每个 IP 每分钟最大上传次数
     */
    private static final int MAX_UPLOADS_PER_MINUTE = 30;

    /**
     * 窗口大小（毫秒）
     */
    private static final long WINDOW_MS = 60_000L;

    /**
     * key = ip, value = 窗口状态
     */
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    /**
     * 检查是否允许上传，超限则抛出 BusinessException
     *
     * @param ip 客户端 IP
     */
    public void checkLimit(String ip) {
        long now = System.currentTimeMillis();
        WindowCounter counter = counters.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                // 新窗口
                return new WindowCounter(now);
            }
            return existing;
        });

        int count = counter.count.incrementAndGet();
        if (count > MAX_UPLOADS_PER_MINUTE) {
            log.warn("上传限流触发: IP={}, 当前窗口请求数={}", ip, count);
            throw new BusinessException("上传操作过于频繁，请稍后再试");
        }
    }

    private static class WindowCounter {
        final long windowStart;
        final AtomicInteger count;

        WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }
    }
}
