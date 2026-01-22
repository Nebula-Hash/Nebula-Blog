package com.nebula.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查响应对象
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
@Builder
public class HealthCheckVO {

    /**
     * 服务整体状态：UP-正常 DOWN-异常
     */
    private String status;

    /**
     * 服务启动时间
     */
    private LocalDateTime startTime;

    /**
     * 运行时长（秒）
     */
    private Long uptime;

    /**
     * 各组件健康状态
     */
    private Map<String, ComponentHealth> components;

    /**
     * 组件健康信息
     */
    @Data
    @Builder
    public static class ComponentHealth {
        /**
         * 组件状态：UP-正常 DOWN-异常
         */
        private String status;

        /**
         * 状态描述
         */
        private String message;

        /**
         * 响应时间（毫秒）
         */
        private Long responseTime;
    }
}
