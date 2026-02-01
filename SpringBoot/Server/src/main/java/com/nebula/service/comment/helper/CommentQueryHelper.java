package com.nebula.service.comment.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogComment;
import com.nebula.entity.BlogCommentLike;
import com.nebula.entity.SysUser;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCommentLikeMapper;
import com.nebula.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论查询辅助类
 * <p>
 * 封装评论模块的批量查询逻辑，避免N+1问题
 *
 * @author Nebula-Hash
 * @date 2026/2/1
 */
@Component
@RequiredArgsConstructor
public class CommentQueryHelper {

    private final SysUserMapper userMapper;
    private final BlogArticleMapper articleMapper;
    private final BlogCommentLikeMapper commentLikeMapper;

    /**
     * 批量获取用户信息Map
     * <p>
     * 从评论列表中提取所有用户ID（包括评论者和被回复者），一次性查询
     *
     * @param comments 评论列表
     * @return 用户ID -> 用户实体 Map
     */
    public Map<Long, SysUser> batchGetUserMap(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = new HashSet<>();
        for (BlogComment comment : comments) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            if (comment.getReplyUserId() != null) {
                userIds.add(comment.getReplyUserId());
            }
        }

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (a, b) -> a));
    }

    /**
     * 批量获取文章信息Map
     * <p>
     * 从评论列表中提取所有文章ID，一次性查询
     *
     * @param comments 评论列表
     * @return 文章ID -> 文章实体 Map
     */
    public Map<Long, BlogArticle> batchGetArticleMap(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> articleIds = comments.stream()
                .map(BlogComment::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return articleMapper.selectBatchIds(articleIds).stream()
                .collect(Collectors.toMap(BlogArticle::getId, article -> article, (a, b) -> a));
    }

    /**
     * 获取当前登录用户已点赞的评论ID集合
     * <p>
     * 未登录时返回空集合
     *
     * @param comments 评论列表
     * @return 已点赞的评论ID集合
     */
    public Set<Long> getCurrentUserLikedCommentIds(List<BlogComment> comments) {
        if (!StpUtil.isLogin()) {
            return Collections.emptySet();
        }

        if (comments == null || comments.isEmpty()) {
            return Collections.emptySet();
        }

        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> commentIds = comments.stream()
                .map(BlogComment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (commentIds.isEmpty()) {
            return Collections.emptySet();
        }

        LambdaQueryWrapper<BlogCommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCommentLike::getUserId, userId)
                .in(BlogCommentLike::getCommentId, commentIds);

        return commentLikeMapper.selectList(wrapper).stream()
                .map(BlogCommentLike::getCommentId)
                .collect(Collectors.toSet());
    }

    /**
     * 根据用户ID集合批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户ID -> 用户实体 Map
     */
    public Map<Long, SysUser> batchGetUserMapByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (a, b) -> a));
    }
}
