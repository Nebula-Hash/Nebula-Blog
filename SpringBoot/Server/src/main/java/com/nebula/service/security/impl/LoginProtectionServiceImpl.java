package com.nebula.service.security.impl;

import com.nebula.exception.BusinessException;
import com.nebula.service.security.LoginProtectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录防护服务实现类
 * <p>
 * 基于 Redis 实现登录失败次数记录和账号锁定功能
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
@Service
@RequiredArgsConstructor
public class LoginProtectionServiceImpl implements LoginProtectionService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 登录失败记录 Redis Key 前缀
     */
    private static final String LOGIN_FAIL_KEY_PREFIX = "nebula:login:fail:";

    /**
     * 最大登录失败次数
     */
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 账号锁定时间（分钟）
     */
    private static final int LOCK_TIME_MINUTES = 30;

    @Override
    public void checkAccountLocked(String username) {
        String key = LOGIN_FAIL_KEY_PREFIX + username;
        String failCountStr = redisTemplate.opsForValue().get(key);
        if (failCountStr != null) {
            int failCount = Integer.parseInt(failCountStr);
            if (failCount >= MAX_LOGIN_ATTEMPTS) {
                Long ttl = redisTemplate.getExpire(key);
                long remainMinutes = ttl != null ? (ttl / 60) + 1 : LOCK_TIME_MINUTES;
                throw new BusinessException("账号已被锁定，请" + remainMinutes + "分钟后再试");
            }
        }
    }

    @Override
    public void recordLoginFail(String username) {
        String key = LOGIN_FAIL_KEY_PREFIX + username;
        // 使用原子操作 increment，避免竞态条件
        Long count = redisTemplate.opsForValue().increment(key);
        // 只在首次失败时设置过期时间，后续只增加计数
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(LOCK_TIME_MINUTES));
        }
    }

    @Override
    public void clearLoginFail(String username) {
        String key = LOGIN_FAIL_KEY_PREFIX + username;
        redisTemplate.delete(key);
    }
}
