package com.nebula.controller;

import com.nebula.result.Result;
import com.nebula.vo.HealthCheckVO;
import com.nebula.vo.VisitStatsVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 * 提供健康检查和访问统计功能
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Slf4j
@RestController
@AllArgsConstructor
public class PingController {

    private final StringRedisTemplate redisTemplate;

    /**
     * 服务启动时间
     */
    private static final LocalDateTime START_TIME = LocalDateTime.now();

    /**
     * Redis Key 前缀
     */
    private static final String VISIT_TOTAL_KEY = "nebula:visit:total";
    private static final String VISIT_DAILY_KEY_PREFIX = "nebula:visit:daily:";

    /**
     * 欢迎页
     */
    @GetMapping("/ping")
    public Result<String> index() {
        // 记录访问
        recordVisit();
        return Result.success("Welcome to Nebula Blog API");
    }

    /**
     * 健康检查接口
     * 检测服务及依赖组件的健康状态
     */
    @GetMapping("/health")
    public Result<HealthCheckVO> health() {
        Map<String, HealthCheckVO.ComponentHealth> components = new HashMap<>(4);

        // 检查 Redis 连接状态
        components.put("redis", checkRedis());

        // 判断整体状态
        boolean allHealthy = components.values().stream()
                .allMatch(c -> "UP".equals(c.getStatus()));

        // 计算运行时长
        long uptimeSeconds = Duration.between(START_TIME, LocalDateTime.now()).getSeconds();

        HealthCheckVO healthCheck = HealthCheckVO.builder()
                .status(allHealthy ? "UP" : "DOWN")
                .startTime(START_TIME)
                .uptime(uptimeSeconds)
                .components(components)
                .build();

        return Result.success(healthCheck);
    }

    /**
     * 访问统计接口
     * 返回总访问量和今日访问量
     */
    @GetMapping("/visits")
    public Result<VisitStatsVO> visitStats() {
        LocalDate today = LocalDate.now();
        String dailyKey = VISIT_DAILY_KEY_PREFIX + today.format(DateTimeFormatter.BASIC_ISO_DATE);

        // 获取总访问量
        String totalStr = redisTemplate.opsForValue().get(VISIT_TOTAL_KEY);
        Long totalVisits = totalStr != null ? Long.parseLong(totalStr) : 0L;

        // 获取今日访问量
        String todayStr = redisTemplate.opsForValue().get(dailyKey);
        Long todayVisits = todayStr != null ? Long.parseLong(todayStr) : 0L;

        VisitStatsVO stats = VisitStatsVO.builder()
                .totalVisits(totalVisits)
                .todayVisits(todayVisits)
                .date(today)
                .timestamp(LocalDateTime.now())
                .build();

        return Result.success(stats);
    }

    /**
     * 记录访问（总量 + 每日）
     */
    private void recordVisit() {
        try {
            // 总访问量自增
            redisTemplate.opsForValue().increment(VISIT_TOTAL_KEY);

            // 每日访问量自增（设置过期时间为2天，便于跨天查询）
            LocalDate today = LocalDate.now();
            String dailyKey = VISIT_DAILY_KEY_PREFIX + today.format(DateTimeFormatter.BASIC_ISO_DATE);
            redisTemplate.opsForValue().increment(dailyKey);
            redisTemplate.expire(dailyKey, Duration.ofDays(2));
        } catch (Exception e) {
            log.warn("记录访问统计失败: {}", e.getMessage());
        }
    }

    /**
     * 检查 Redis 连接状态
     */
    private HealthCheckVO.ComponentHealth checkRedis() {
        long startTime = System.currentTimeMillis();
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            long responseTime = System.currentTimeMillis() - startTime;

            return HealthCheckVO.ComponentHealth.builder()
                    .status("UP")
                    .message("PONG".equals(pong) ? "连接正常" : pong)
                    .responseTime(responseTime)
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Redis 健康检查失败", e);

            return HealthCheckVO.ComponentHealth.builder()
                    .status("DOWN")
                    .message("连接失败: " + e.getMessage())
                    .responseTime(responseTime)
                    .build();
        }
    }
}
