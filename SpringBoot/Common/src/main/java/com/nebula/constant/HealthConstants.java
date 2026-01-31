package com.nebula.constant;

/**
 * 健康检查相关常量
 * <p>
 * 统一管理健康检查状态、响应等相关常量
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class HealthConstants {

    private HealthConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * 健康状态 - 正常
     */
    public static final String STATUS_UP = "UP";

    /**
     * 健康状态 - 异常
     */
    public static final String STATUS_DOWN = "DOWN";

    /**
     * Redis PING 响应
     */
    public static final String REDIS_PONG = "PONG";
}
