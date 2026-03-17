package com.nebula.service.comment.helper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogComment;
import com.nebula.enumeration.AuditStatusEnum;
import com.nebula.mapper.BlogArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论计数辅助类
 * <p>
 * 封装文章评论数的增减逻辑，使用原子SQL避免并发问题
 *
 * @author Nebula-Hash
 * @date 2026/2/1
 */
@Component
@RequiredArgsConstructor
public class CommentCountHelper {

    private final BlogArticleMapper articleMapper;

    /**
     * 增加文章评论数
     *
     * @param articleId 文章ID
     * @param increment 增加数量
     */
    public void incrementCommentCount(Long articleId, int increment) {
        if (articleId == null || increment <= 0) {
            return;
        }

        articleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                .eq(BlogArticle::getId, articleId)
                .setSql("comment_count = comment_count + " + increment));
    }

    /**
     * 减少文章评论数
     * <p>
     * 使用GREATEST函数确保评论数不会变成负数
     *
     * @param articleId 文章ID
     * @param decrement 减少数量
     */
    public void decrementCommentCount(Long articleId, int decrement) {
        if (articleId == null || decrement <= 0) {
            return;
        }

        articleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                .eq(BlogArticle::getId, articleId)
                .setSql("comment_count = GREATEST(comment_count - " + decrement + ", 0)"));
    }

    /**
     * 批量增加文章评论数
     * <p>
     * 按文章分组更新，减少数据库操作次数
     *
     * @param comments 评论列表（仅统计审核通过的）
     */
    public void batchIncrementCommentCount(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        // 按文章ID分组统计数量
        Map<Long, Long> articleCountMap = comments.stream()
                .filter(c -> AuditStatusEnum.isApproved(c.getAuditStatus()))
                .collect(Collectors.groupingBy(BlogComment::getArticleId, Collectors.counting()));

        // 批量更新文章评论数
        for (Map.Entry<Long, Long> entry : articleCountMap.entrySet()) {
            incrementCommentCount(entry.getKey(), entry.getValue().intValue());
        }
    }

    /**
     * 批量减少文章评论数
     * <p>
     * 按文章分组更新，减少数据库操作次数
     *
     * @param comments 评论列表（仅统计审核通过的）
     */
    public void batchDecrementCommentCount(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        // 按文章ID分组统计数量
        Map<Long, Long> articleCountMap = comments.stream()
                .filter(c -> AuditStatusEnum.isApproved(c.getAuditStatus()))
                .collect(Collectors.groupingBy(BlogComment::getArticleId, Collectors.counting()));

        // 批量更新文章评论数
        for (Map.Entry<Long, Long> entry : articleCountMap.entrySet()) {
            decrementCommentCount(entry.getKey(), entry.getValue().intValue());
        }
    }

    /**
     * 计算评论列表中审核通过的评论数量
     *
     * @param comments 评论列表
     * @return 审核通过的评论数量
     */
    public int countApprovedComments(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        return (int) comments.stream()
                .filter(c -> AuditStatusEnum.isApproved(c.getAuditStatus()))
                .count();
    }

    /**
     * 判断评论是否计入文章评论数
     * <p>
     * 只有审核通过的评论才计入
     *
     * @param comment 评论实体
     * @return true-计入，false-不计入
     */
    public boolean shouldCountComment(BlogComment comment) {
        return comment != null && AuditStatusEnum.isApproved(comment.getAuditStatus());
    }
}
