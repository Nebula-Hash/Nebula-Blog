package com.nebula.config;

import com.nebula.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 上传接口限流器
 * 优先使用 Redis 进行多实例一致限流，Redis 不可用时降级为本地内存限流
 *
 * @author Nebula-Hash
 * @date 2026/2/25
 */
@Slf4j
@Component
public class UploadRateLimiter {

    private static final int MAX_UPLOADS_PER_MINUTE = 30;
    private static final long WINDOW_MS = 60_000L;
    private static final String REDIS_KEY_PREFIX = "nebula:upload:limit:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis 不可用时的本地兜底计数器
     */
    private final ConcurrentHashMap<String, WindowCounter> localCounters = new ConcurrentHashMap<>();

    /**
     * 构造上传限流器
     *
     * @param stringRedisTemplate Redis 模板
     */
    public UploadRateLimiter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 按请求来源 IP 进行限流校验
     *
     * @param request HTTP 请求
     */
    public void checkLimit(HttpServletRequest request) {
        checkLimit(resolveClientIp(request));
    }

    /**
     * 按指定 IP 进行限流校验
     *
     * @param ip 客户端 IP
     */
    public void checkLimit(String ip) {
        String clientIp = StringUtils.hasText(ip) ? ip : "unknown";
        if (tryCheckWithRedis(clientIp)) {
            return;
        }
        checkWithLocalCounter(clientIp);
    }

    /**
     * 使用 Redis 进行限流校验
     *
     * @param ip 客户端 IP
     * @return true 表示已由 Redis 完成限流校验，false 表示需要降级到本地校验
     */
    private boolean tryCheckWithRedis(String ip) {
        try {
            Instant now = Instant.now();
            long currentMinute = now.truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
            String key = REDIS_KEY_PREFIX + ip + ":" + currentMinute;

            Long current = stringRedisTemplate.opsForValue().increment(key);
            if (current == null) {
                return false;
            }

            if (current == 1L) {
                stringRedisTemplate.expire(key, Duration.ofMinutes(2));
            }

            if (current > MAX_UPLOADS_PER_MINUTE) {
                throw new BusinessException("上传操作过于频繁，请稍后再试");
            }
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 限流不可用，降级到本地限流", e);
            return false;
        }
    }

    /**
     * 使用本地内存计数器进行限流
     *
     * @param ip 客户端 IP
     */
    private void checkWithLocalCounter(String ip) {
        long now = System.currentTimeMillis();
        WindowCounter counter = localCounters.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new WindowCounter(now);
            }
            return existing;
        });

        int count = counter.count.incrementAndGet();
        if (count > MAX_UPLOADS_PER_MINUTE) {
            throw new BusinessException("上传操作过于频繁，请稍后再试");
        }

        // 当映射规模过大时，顺带清理过期窗口，避免本地缓存无限增长
        if (localCounters.size() > 10_000) {
            cleanupExpiredWindows(now);
        }
    }

    /**
     * 清理过期的本地限流窗口
     *
     * @param now 当前时间戳（毫秒）
     */
    private void cleanupExpiredWindows(long now) {
        for (Map.Entry<String, WindowCounter> entry : localCounters.entrySet()) {
            if (now - entry.getValue().windowStart > WINDOW_MS * 2) {
                localCounters.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 从请求中解析客户端 IP
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String value = request.getHeader(header);
            if (!StringUtils.hasText(value) || "unknown".equalsIgnoreCase(value)) {
                continue;
            }
            if (value.contains(",")) {
                return value.split(",")[0].trim();
            }
            return value.trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * 限流时间窗口计数器
     */
    private static class WindowCounter {

        final long windowStart;
        final AtomicInteger count;

        /**
         * 构造窗口计数器
         *
         * @param windowStart 窗口开始时间
         */
        WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }
    }
}
