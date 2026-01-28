package com.nebula.service.authority.security.impl;

import com.nebula.exception.BusinessException;
import com.nebula.service.authority.helper.AuthHelper;
import com.nebula.service.authority.security.RegisterLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    @Override
    public void checkRegisterLimit(String ip) {
        String key = AuthHelper.REGISTER_LIMIT_KEY_PREFIX + ip;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr != null) {
            int count = Integer.parseInt(countStr);
            if (count >= AuthHelper.MAX_REGISTER_PER_HOUR) {
                throw new BusinessException("注册过于频繁，请稍后再试");
            }
        }
    }

    @Override
    public void recordRegister(String ip) {
        String key = AuthHelper.REGISTER_LIMIT_KEY_PREFIX + ip;
        // 使用原子操作 increment，避免竞态条件
        Long count = redisTemplate.opsForValue().increment(key);
        // 只在首次注册时设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofHours(AuthHelper.REGISTER_LIMIT_HOURS));
        }
    }
}
