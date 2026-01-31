package com.nebula.service.article.helper;

import com.nebula.entity.BlogArticle;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * 热门文章评分计算工具
 * <p>
 * 使用综合评分算法计算文章热度：
 * <pre>
 * 热度评分 = 基础分数 × 时间衰减因子
 * 基础分数 = 浏览量×w1 + 点赞数×w2 + 评论数×w3 + 收藏数×w4
 * 时间衰减 = 0.5^(发布天数 / 半衰期)
 * </pre>
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Component
public class HotArticleScoreHelper {

    // ==================== 权重配置 ====================

    /**
     * 浏览量权重 - 基础指标
     */
    private static final double WEIGHT_VIEW = 1.0;

    /**
     * 点赞数权重 - 主动互动，价值较高
     */
    private static final double WEIGHT_LIKE = 5.0;

    /**
     * 评论数权重 - 深度互动，价值最高
     */
    private static final double WEIGHT_COMMENT = 10.0;

    /**
     * 收藏数权重 - 高价值互动
     */
    private static final double WEIGHT_COLLECT = 8.0;

    // ==================== 时间衰减配置 ====================

    /**
     * 热度半衰期（天）- 7天后热度衰减50%
     */
    private static final double HALF_LIFE_DAYS = 7.0;

    /**
     * 衰减基数（用于计算指数衰减）
     */
    private static final double DECAY_BASE = 0.5;

    /**
     * 最小衰减因子 - 防止老文章热度归零
     */
    private static final double MIN_DECAY_FACTOR = 0.01;

    /**
     * 计算文章热度评分
     *
     * @param article 文章实体
     * @return 热度评分
     */
    public double calculateScore(BlogArticle article) {
        if (article == null) {
            return 0.0;
        }

        // 计算基础分数
        double baseScore = calculateBaseScore(article);

        // 计算时间衰减因子
        double decayFactor = calculateDecayFactor(article.getCreateTime());

        return baseScore * decayFactor;
    }

    /**
     * 计算基础分数（不含时间衰减）
     *
     * @param article 文章实体
     * @return 基础分数
     */
    public double calculateBaseScore(BlogArticle article) {
        int viewCount = getValueOrZero(article.getViewCount());
        int likeCount = getValueOrZero(article.getLikeCount());
        int commentCount = getValueOrZero(article.getCommentCount());
        int collectCount = getValueOrZero(article.getCollectCount());

        return viewCount * WEIGHT_VIEW
                + likeCount * WEIGHT_LIKE
                + commentCount * WEIGHT_COMMENT
                + collectCount * WEIGHT_COLLECT;
    }

    /**
     * 计算时间衰减因子
     * <p>
     * 使用指数衰减模型: decay = 0.5^(age / halfLife)
     *
     * @param createTime 文章创建时间
     * @return 衰减因子 (0, 1]
     */
    public double calculateDecayFactor(LocalDateTime createTime) {
        if (createTime == null) {
            return 1.0;
        }

        long daysOld = ChronoUnit.DAYS.between(createTime, LocalDateTime.now());
        if (daysOld <= 0) {
            return 1.0;
        }

        double decayFactor = Math.pow(DECAY_BASE, daysOld / HALF_LIFE_DAYS);
        return Math.max(decayFactor, MIN_DECAY_FACTOR);
    }

    /**
     * 对文章列表按热度评分排序（降序）
     *
     * @param articles 文章列表
     * @return 排序后的列表（原列表不变）
     */
    public List<BlogArticle> sortByScore(List<BlogArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return articles;
        }

        return articles.stream()
                .sorted(Comparator.comparingDouble(this::calculateScore).reversed())
                .toList();
    }

    /**
     * 对文章列表按热度评分排序并取前N条
     *
     * @param articles 文章列表
     * @param limit    数量限制
     * @return 热度最高的N篇文章
     */
    public List<BlogArticle> getTopHotArticles(List<BlogArticle> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            return articles;
        }

        return articles.stream()
                .sorted(Comparator.comparingDouble(this::calculateScore).reversed())
                .limit(limit)
                .toList();
    }

    /**
     * 安全获取数值，null转为0
     */
    private int getValueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
