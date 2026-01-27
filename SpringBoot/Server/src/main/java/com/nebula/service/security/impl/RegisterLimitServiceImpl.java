package com.nebula.service.security.impl;

import com.nebula.exception.BusinessException;
import com.nebula.service.security.RegisterLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 注册限流服务实现类
 * <p>
 * 基于 Redis 实现 IP 注册频率限制
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
@Service
@RequiredArgsConstructor
public class RegisterLimitServiceImpl implements RegisterLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 注册限制 Redis Key 前缀
     */
    private static final String REGISTER_LIMIT_KEY_PREFIX = "nebula:register:limit:";

    /**
     * 每小时最大注册次数（同IP）
     */
    private static final int MAX_REGISTER_PER_HOUR = 5;

    /**
     * 注册限制时间（小时）
     */
    private static final int REGISTER_LIMIT_HOURS = 1;

    @Override
    public void checkRegisterLimit(String ip) {
        String key = REGISTER_LIMIT_KEY_PREFIX + ip;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr != null) {
            int count = Integer.parseInt(countStr);
            if (count >= MAX_REGISTER_PER_HOUR) {
                throw new BusinessException("注册过于频繁，请稍后再试");
            }
        }
    }

    @Override
    public void recordRegister(String ip) {
        String key = REGISTER_LIMIT_KEY_PREFIX + ip;
        // 使用原子操作 increment，避免竞态条件
        Long count = redisTemplate.opsForValue().increment(key);
        // 只在首次注册时设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofHours(REGISTER_LIMIT_HOURS));
        }
    }

    @Override
    public String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();

        // 优先从 X-Forwarded-For 获取（适用于反向代理场景）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}
