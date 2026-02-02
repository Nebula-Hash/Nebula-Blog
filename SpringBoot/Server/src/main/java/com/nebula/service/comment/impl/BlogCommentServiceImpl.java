package com.nebula.service.comment.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.CommentConstants;
import com.nebula.constant.CountConstants;
import com.nebula.dto.CommentDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogComment;
import com.nebula.entity.BlogCommentLike;
import com.nebula.entity.SysUser;
import com.nebula.enumeration.AuditStatusEnum;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCommentLikeMapper;
import com.nebula.mapper.BlogCommentMapper;
import com.nebula.service.comment.BlogCommentService;
import com.nebula.service.comment.converter.CommentConverter;
import com.nebula.service.comment.helper.CommentCountHelper;
import com.nebula.service.comment.helper.CommentQueryHelper;
import com.nebula.vo.admin.CommentAdminVO;
import com.nebula.vo.client.CommentClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * <p>
 * 职责：编排评论业务逻辑，具体的转换和查询逻辑委托给辅助类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogCommentLikeMapper commentLikeMapper;
    private final BlogArticleMapper articleMapper;

    private final CommentConverter converter;
    private final CommentCountHelper countHelper;
    private final CommentQueryHelper queryHelper;

    // ==================== 客户端方法 ====================

    /**
     * 获取文章评论列表
     */
    @Override
    public Page<CommentClientVO> getArticleComments(Long articleId, Long current, Long size) {
        // 1. 分页查询根评论
        Page<BlogComment> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogComment> rootWrapper = new LambdaQueryWrapper<>();
        rootWrapper.eq(BlogComment::getArticleId, articleId)
                .isNull(BlogComment::getRootId)
                .eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByDesc(BlogComment::getCreateTime);

        Page<BlogComment> rootPage = commentMapper.selectPage(page, rootWrapper);
        List<BlogComment> rootComments = rootPage.getRecords();

        if (rootComments.isEmpty()) {
            return buildEmptyPage(current, size, rootPage.getTotal());
        }

        // 2. 批量查询所有回复
        List<Long> rootIds = rootComments.stream()
                .map(BlogComment::getId)
                .collect(Collectors.toList());
        List<BlogComment> allReplies = queryRepliesByRootIds(rootIds);

        // 3. 使用转换器组装结果
        return converter.toClientVOPage(rootPage, rootComments, allReplies, current, size);
    }

    /**
     * 发布评论
     * TODO: 后续添加评论审核功能，目前默认发布即审核通过
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(CommentDTO commentDTO) {
        // 1. 校验文章是否存在
        BlogArticle article = articleMapper.selectById(commentDTO.getArticleId());
        if (article == null) {
            throw new BusinessException(CommentConstants.ERR_ARTICLE_NOT_FOUND);
        }

        // 2. 构建评论实体
        BlogComment comment = buildComment(commentDTO);

        // 3. 处理回复关系（计算rootId）
        processReplyRelation(comment, commentDTO.getParentId());

        // 4. 保存评论
        commentMapper.insert(comment);

        // 5. 更新文章评论数
        countHelper.incrementCommentCount(article.getId(), 1);

        return comment.getId();
    }

    /**
     * 删除自己的评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyComment(Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();

        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        // 校验是否是自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(CommentConstants.ERR_NO_PERMISSION_DELETE);
        }

        // 复用管理端删除逻辑（级联删除子评论 + 更新计数）
        deleteComment(commentId);
    }

    /**
     * 点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 检查评论是否存在
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        // 检查是否已点赞
        LambdaQueryWrapper<BlogCommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCommentLike::getCommentId, commentId)
                .eq(BlogCommentLike::getUserId, userId);
        BlogCommentLike existLike = commentLikeMapper.selectOne(wrapper);

        if (existLike != null) {
            // 取消点赞
            commentLikeMapper.deleteById(existLike.getId());
            updateLikeCount(commentId, -1);
        } else {
            // 点赞
            BlogCommentLike like = new BlogCommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            updateLikeCount(commentId, 1);
        }
    }

    /**
     * 获取评论的回复列表
     */
    @Override
    public Page<CommentClientVO> getReplies(Long rootId, Long current, Long size) {
        // 验证根评论是否存在
        BlogComment rootComment = commentMapper.selectById(rootId);
        if (rootComment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        // 分页查询该根评论下的所有回复
        Page<BlogComment> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getRootId, rootId)
                .eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByAsc(BlogComment::getCreateTime);

        Page<BlogComment> replyPage = commentMapper.selectPage(page, wrapper);
        List<BlogComment> replies = replyPage.getRecords();

        if (replies.isEmpty()) {
            return buildEmptyPage(current, size, replyPage.getTotal());
        }

        // 批量获取用户信息和点赞状态
        Map<Long, SysUser> userMap = queryHelper.batchGetUserMap(replies);
        Set<Long> likedCommentIds = queryHelper.getCurrentUserLikedCommentIds(replies);

        // 转换为VO
        List<CommentClientVO> voList = replies.stream()
                .map(reply -> converter.toClientVO(reply, userMap, likedCommentIds))
                .collect(Collectors.toList());

        Page<CommentClientVO> voPage = new Page<>(current, size);
        voPage.setTotal(replyPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }


    // ==================== 管理端方法 ====================

    /**
     * 获取后台评论列表
     */
    @Override
    public Page<CommentAdminVO> getAdminCommentList(Long current, Long size, Long articleId,
                                                     Long userId, Integer auditStatus, String keyword) {
        Page<BlogComment> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();

        // 条件筛选
        wrapper.eq(articleId != null, BlogComment::getArticleId, articleId)
                .eq(userId != null, BlogComment::getUserId, userId)
                .eq(auditStatus != null, BlogComment::getAuditStatus, auditStatus)
                .like(StringUtils.hasText(keyword), BlogComment::getContent, keyword)
                .orderByDesc(BlogComment::getCreateTime);

        Page<BlogComment> commentPage = commentMapper.selectPage(page, wrapper);

        if (commentPage.getRecords().isEmpty()) {
            return buildEmptyAdminPage(current, size);
        }

        return converter.toAdminVOPage(commentPage, current, size);
    }

    /**
     * 审核评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long commentId, Integer auditStatus) {
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        validateAuditStatus(auditStatus);

        Integer currentStatus = comment.getAuditStatus();

        // 检查是否已经是目标状态（避免重复审核）
        if (currentStatus.equals(auditStatus)) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_ALREADY_AUDITED);
        }

        // 只允许审核待审核状态的评论
        if (!AuditStatusEnum.isPending(currentStatus)) {
            throw new BusinessException(CommentConstants.ERR_ONLY_AUDIT_PENDING);
        }

        // 使用条件更新确保原子性
        LambdaUpdateWrapper<BlogComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BlogComment::getId, commentId)
                .eq(BlogComment::getAuditStatus, AuditStatusEnum.PENDING.getCode())
                .set(BlogComment::getAuditStatus, auditStatus);

        int updateCount = commentMapper.update(null, updateWrapper);

        // 如果更新失败，说明状态已被其他操作修改
        if (updateCount == 0) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_ALREADY_AUDITED);
        }

        // 审核通过时，文章评论数+1
        if (AuditStatusEnum.isApproved(auditStatus)) {
            countHelper.incrementCommentCount(comment.getArticleId(), 1);
        }
    }

    /**
     * 删除评论（级联删除子评论）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        Long articleId = comment.getArticleId();
        int deleteCount = 0;

        // 如果是根评论，级联删除所有子评论
        if (comment.getRootId() == null) {
            deleteCount += deleteChildComments(commentId);
        }

        // 统计当前评论
        if (countHelper.shouldCountComment(comment)) {
            deleteCount += 1;
        }

        // 删除当前评论的点赞记录
        deleteCommentLikes(commentId);

        // 删除当前评论
        commentMapper.deleteById(commentId);

        // 更新文章评论数
        countHelper.decrementCommentCount(articleId, deleteCount);
    }

    /**
     * 批量审核评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchAuditResultVO batchAuditComments(List<Long> commentIds, Integer auditStatus) {
        if (CollectionUtils.isEmpty(commentIds)) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_IDS_EMPTY);
        }
        validateAuditStatus(auditStatus);

        int totalCount = commentIds.size();

        // 使用条件更新确保原子性：只更新待审核状态的评论
        LambdaUpdateWrapper<BlogComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BlogComment::getId, commentIds)
                .eq(BlogComment::getAuditStatus, AuditStatusEnum.PENDING.getCode())
                .set(BlogComment::getAuditStatus, auditStatus);

        int successCount = commentMapper.update(null, updateWrapper);
        int skippedCount = totalCount - successCount;

        // 如果没有更新任何记录，说明所有评论都不是待审核状态
        if (successCount == 0) {
            throw new BusinessException(CommentConstants.ERR_ONLY_AUDIT_PENDING);
        }

        // 审核通过时，查询实际更新成功的评论并更新文章评论数
        if (AuditStatusEnum.isApproved(auditStatus)) {
            // 查询刚刚更新成功的评论（状态已变为通过）
            LambdaQueryWrapper<BlogComment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(BlogComment::getId, commentIds)
                    .eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode());
            List<BlogComment> approvedComments = commentMapper.selectList(queryWrapper);

            // 批量更新文章评论数
            countHelper.batchIncrementCommentCount(approvedComments);
        }

        String message = String.format("成功审核 %d 条评论，跳过 %d 条（非待审核状态）", successCount, skippedCount);
        return BatchAuditResultVO.success(successCount, skippedCount, totalCount, message);
    }

    /**
     * 批量删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchDeleteResultVO batchDeleteComments(List<Long> commentIds) {
        if (CollectionUtils.isEmpty(commentIds)) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_IDS_EMPTY);
        }

        // 1. 查询所有要删除的评论
        LambdaQueryWrapper<BlogComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BlogComment::getId, commentIds);
        List<BlogComment> commentsToDelete = commentMapper.selectList(queryWrapper);

        int notFoundCount = commentIds.size() - commentsToDelete.size();

        if (commentsToDelete.isEmpty()) {
            String message = String.format("所有评论均不存在，跳过 %d 条", notFoundCount);
            return BatchDeleteResultVO.success(0, 0, notFoundCount, message);
        }

        // 2. 找出所有根评论ID，查询它们的子评论
        List<Long> rootCommentIds = commentsToDelete.stream()
                .filter(c -> c.getRootId() == null)
                .map(BlogComment::getId)
                .collect(Collectors.toList());

        List<BlogComment> childComments = Collections.emptyList();
        if (!rootCommentIds.isEmpty()) {
            LambdaQueryWrapper<BlogComment> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.in(BlogComment::getRootId, rootCommentIds);
            childComments = commentMapper.selectList(childWrapper);
        }

        int rootCount = commentsToDelete.size();
        int childCount = childComments.size();

        // 3. 合并所有要删除的评论（包括子评论）
        List<BlogComment> allCommentsToDelete = new ArrayList<>(commentsToDelete);
        allCommentsToDelete.addAll(childComments);

        // 4. 按文章ID分组，统计每篇文章需要减少的审核通过评论数
        Map<Long, Long> articleDecrementMap = allCommentsToDelete.stream()
                .filter(c -> AuditStatusEnum.isApproved(c.getAuditStatus()))
                .collect(Collectors.groupingBy(BlogComment::getArticleId, Collectors.counting()));

        // 5. 收集所有要删除的评论ID（包括子评论）
        List<Long> allIdsToDelete = allCommentsToDelete.stream()
                .map(BlogComment::getId)
                .collect(Collectors.toList());

        // 6. 批量删除点赞记录
        if (!allIdsToDelete.isEmpty()) {
            batchDeleteCommentLikes(allIdsToDelete);
        }

        // 7. 批量删除评论
        if (!allIdsToDelete.isEmpty()) {
            commentMapper.deleteBatchIds(allIdsToDelete);
        }

        // 8. 批量更新文章评论数
        for (Map.Entry<Long, Long> entry : articleDecrementMap.entrySet()) {
            countHelper.decrementCommentCount(entry.getKey(), entry.getValue().intValue());
        }

        String message = String.format("成功删除 %d 条根评论和 %d 条子评论，跳过 %d 条（不存在）",
                rootCount, childCount, notFoundCount);
        return BatchDeleteResultVO.success(rootCount, childCount, notFoundCount, message);
    }

    /**
     * 获取待审核评论数
     */
    @Override
    public Long getPendingAuditCount() {
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getAuditStatus, AuditStatusEnum.PENDING.getCode());
        return commentMapper.selectCount(wrapper);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建评论实体
     */
    private BlogComment buildComment(CommentDTO commentDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        BlogComment comment = new BlogComment();
        BeanUtils.copyProperties(commentDTO, comment);
        comment.setUserId(userId);
        comment.setLikeCount(CountConstants.INIT_VALUE);
        comment.setAuditStatus(AuditStatusEnum.APPROVED.getCode());
        comment.setContent(HtmlUtils.htmlEscape(commentDTO.getContent()));

        return comment;
    }

    /**
     * 处理回复关系，计算rootId并校验replyUserId
     */
    private void processReplyRelation(BlogComment comment, Long parentId) {
        if (parentId == null) {
            return;
        }

        BlogComment parentComment = commentMapper.selectById(parentId);
        if (parentComment == null) {
            throw new BusinessException(CommentConstants.ERR_PARENT_COMMENT_NOT_FOUND);
        }

        // 校验被回复用户：如果前端传了replyUserId，必须与父评论作者一致
        if (comment.getReplyUserId() != null) {
            if (!comment.getReplyUserId().equals(parentComment.getUserId())) {
                throw new BusinessException(CommentConstants.ERR_REPLY_USER_MISMATCH);
            }
        } else {
            // 如果未指定，默认回复父评论作者
            comment.setReplyUserId(parentComment.getUserId());
        }

        // 计算rootId：如果父评论是根评论，则rootId = 父评论ID
        // 如果父评论不是根评论，则rootId = 父评论的rootId
        comment.setRootId(parentComment.getRootId() != null
                ? parentComment.getRootId()
                : parentComment.getId());
    }

    /**
     * 更新评论点赞数
     */
    private void updateLikeCount(Long commentId, int delta) {
        if (delta > 0) {
            commentMapper.update(null, new LambdaUpdateWrapper<BlogComment>()
                    .eq(BlogComment::getId, commentId)
                    .setSql("like_count = like_count + " + delta));
        } else {
            commentMapper.update(null, new LambdaUpdateWrapper<BlogComment>()
                    .eq(BlogComment::getId, commentId)
                    .setSql("like_count = GREATEST(like_count - " + Math.abs(delta) + ", 0)"));
        }
    }

    /**
     * 根据根评论ID列表查询所有回复（带数量限制）
     * <p>
     * 使用 MyBatis-Plus 分页限制总查询数量，避免单个热门评论回复过多导致性能问题
     */
    private List<BlogComment> queryRepliesByRootIds(List<Long> rootIds) {
        if (rootIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算分页大小：每个根评论最多加载 MAX_REPLIES_PER_ROOT 条回复
        int maxSize = CommentConstants.MAX_REPLIES_PER_ROOT * rootIds.size();
        Page<BlogComment> page = new Page<>(1, maxSize, false);

        LambdaQueryWrapper<BlogComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(BlogComment::getRootId, rootIds)
                .eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByAsc(BlogComment::getCreateTime);

        return commentMapper.selectPage(page, replyWrapper).getRecords();
    }

    /**
     * 删除子评论并返回删除的审核通过评论数量
     */
    private int deleteChildComments(Long rootCommentId) {
        LambdaQueryWrapper<BlogComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(BlogComment::getRootId, rootCommentId);
        List<BlogComment> childComments = commentMapper.selectList(childWrapper);

        if (childComments.isEmpty()) {
            return 0;
        }

        // 统计审核通过的子评论数量
        int approvedCount = countHelper.countApprovedComments(childComments);

        // 收集所有子评论ID
        List<Long> childIds = childComments.stream()
                .map(BlogComment::getId)
                .collect(Collectors.toList());

        // 批量删除子评论的点赞记录
        batchDeleteCommentLikes(childIds);

        // 删除所有子评论
        LambdaUpdateWrapper<BlogComment> deleteWrapper = new LambdaUpdateWrapper<>();
        deleteWrapper.eq(BlogComment::getRootId, rootCommentId);
        commentMapper.delete(deleteWrapper);

        return approvedCount;
    }

    /**
     * 删除单个评论的点赞记录
     */
    private void deleteCommentLikes(Long commentId) {
        LambdaQueryWrapper<BlogCommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCommentLike::getCommentId, commentId);
        commentLikeMapper.delete(wrapper);
    }

    /**
     * 批量删除评论的点赞记录
     */
    private void batchDeleteCommentLikes(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<BlogCommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BlogCommentLike::getCommentId, commentIds);
        commentLikeMapper.delete(wrapper);
    }

    /**
     * 验证审核状态
     */
    private void validateAuditStatus(Integer auditStatus) {
        if (!AuditStatusEnum.isApproved(auditStatus) && !AuditStatusEnum.isRejected(auditStatus)) {
            throw new BusinessException(CommentConstants.ERR_INVALID_AUDIT_STATUS);
        }
    }

    /**
     * 构建空的客户端评论分页
     */
    private Page<CommentClientVO> buildEmptyPage(Long current, Long size, Long total) {
        Page<CommentClientVO> emptyPage = new Page<>(current, size);
        emptyPage.setTotal(total);
        emptyPage.setRecords(Collections.emptyList());
        return emptyPage;
    }

    /**
     * 构建空的管理端评论分页
     */
    private Page<CommentAdminVO> buildEmptyAdminPage(Long current, Long size) {
        Page<CommentAdminVO> emptyPage = new Page<>(current, size);
        emptyPage.setTotal(0L);
        emptyPage.setRecords(Collections.emptyList());
        return emptyPage;
    }
}
