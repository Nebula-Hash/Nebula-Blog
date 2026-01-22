package com.nebula.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访问统计响应对象
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
@Builder
public class VisitStatsVO {

    /**
     * 总访问量
     */
    private Long totalVisits;

    /**
     * 今日访问量
     */
    private Long todayVisits;

    /**
     * 统计日期
     */
    private LocalDate date;

    /**
     * 统计时间
     */
    private LocalDateTime timestamp;
}
