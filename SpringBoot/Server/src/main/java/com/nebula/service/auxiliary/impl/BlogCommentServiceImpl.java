package com.nebula.service.auxiliary.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.CommentConstants;
import com.nebula.constant.CountConstants;
import com.nebula.dto.CommentDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogComment;
import com.nebula.entity.SysUser;
import com.nebula.enumeration.AuditStatusEnum;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCommentMapper;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.auxiliary.BlogCommentService;
import com.nebula.vo.CommentAdminVO;
import com.nebula.vo.CommentClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogArticleMapper articleMapper;
    private final SysUserMapper userMapper;

    // ==================== 客户端方法 ====================

    /**
     * 发布评论
     * TODO: 后续添加评论审核功能，目前默认发布即审核通过
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(CommentDTO commentDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        BlogComment comment = new BlogComment();
        BeanUtils.copyProperties(commentDTO, comment);
        comment.setUserId(userId);
        comment.setLikeCount(CountConstants.INIT_VALUE);
        // TODO: 后续修改为 AuditStatusEnum.PENDING.getCode()，需要审核后才展示
        comment.setAuditStatus(AuditStatusEnum.APPROVED.getCode());

        // 自动计算 rootId
        if (commentDTO.getParentId() != null) {
            BlogComment parentComment = commentMapper.selectById(commentDTO.getParentId());
            if (parentComment != null) {
                // 如果父评论是根评论，则 rootId = 父评论ID
                // 如果父评论不是根评论，则 rootId = 父评论的 rootId
                comment.setRootId(parentComment.getRootId() != null 
                        ? parentComment.getRootId() 
                        : parentComment.getId());
            }
        }
        // 根评论时 rootId 保持为 null

        commentMapper.insert(comment);

        // 更新文章评论数
        BlogArticle article = articleMapper.selectById(commentDTO.getArticleId());
        if (article != null) {
            article.setCommentCount(article.getCommentCount() + CountConstants.INCREMENT);
            articleMapper.updateById(article);
        }

        return comment.getId();
    }

    @Override
    public void likeComment(Long commentId) {
        // 简化实现，直接更新点赞数
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + CountConstants.INCREMENT);
            commentMapper.updateById(comment);
        }
    }

    /**
     * 获取文章评论列表（楼中楼结构）
     * <p>
     * 查询逻辑：
     * 1. 分页查询根评论（rootId IS NULL）
     * 2. 批量查询每个根评论下的所有回复（通过 rootId 一次查询）
     * 3. 扁平化展示，通过 @用户名 标识回复关系
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
            Page<CommentClientVO> emptyPage = new Page<>(current, size);
            emptyPage.setTotal(rootPage.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        // 2. 批量查询所有根评论下的回复
        List<Long> rootIds = rootComments.stream()
                .map(BlogComment::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<BlogComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(BlogComment::getRootId, rootIds)
                .eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByAsc(BlogComment::getCreateTime);

        List<BlogComment> allReplies = commentMapper.selectList(replyWrapper);

        // 3. 按 rootId 分组
        Map<Long, List<BlogComment>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(BlogComment::getRootId));

        // 4. 组装结果
        List<CommentClientVO> voList = new ArrayList<>();
        for (BlogComment rootComment : rootComments) {
            CommentClientVO rootVO = convertToVO(rootComment);
            
            // 获取该根评论下的所有回复，作为 children 扁平展示
            List<BlogComment> replies = replyMap.getOrDefault(rootComment.getId(), Collections.emptyList());
            List<CommentClientVO> childrenVOs = replies.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            rootVO.setChildren(childrenVOs);
            
            voList.add(rootVO);
        }

        Page<CommentClientVO> voPage = new Page<>(current, size);
        voPage.setTotal(rootPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 将评论实体转换为VO
     */
    private CommentClientVO convertToVO(BlogComment comment) {
        CommentClientVO vo = new CommentClientVO();
        BeanUtils.copyProperties(comment, vo);

        // 获取评论用户信息
        SysUser user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        // 获取被回复用户信息
        if (comment.getReplyUserId() != null) {
            SysUser replyUser = userMapper.selectById(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyNickname(replyUser.getNickname());
            }
        }

        vo.setIsLiked(false);
        return vo;
    }

    // ==================== 管理端方法 ====================

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

        // 转换为 AdminVO
        List<CommentAdminVO> voList = commentPage.getRecords().stream()
                .map(this::convertToAdminVO)
                .collect(Collectors.toList());

        Page<CommentAdminVO> voPage = new Page<>(current, size);
        voPage.setTotal(commentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long commentId, Integer auditStatus) {
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        // 验证审核状态
        if (!AuditStatusEnum.isApproved(auditStatus) && !AuditStatusEnum.isRejected(auditStatus)) {
            throw new BusinessException(CommentConstants.ERR_INVALID_AUDIT_STATUS);
        }

        Integer oldStatus = comment.getAuditStatus();
        comment.setAuditStatus(auditStatus);
        commentMapper.updateById(comment);

        // 审核通过且原状态为待审核时，文章评论数+1
        if (AuditStatusEnum.isApproved(auditStatus) && AuditStatusEnum.isPending(oldStatus)) {
            BlogArticle article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                article.setCommentCount(article.getCommentCount() + CountConstants.INCREMENT);
                articleMapper.updateById(article);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_NOT_FOUND);
        }

        // 计算需要减少的评论数（只计算已审核通过的）
        int deleteCount = 0;
        if (AuditStatusEnum.isApproved(comment.getAuditStatus())) {
            deleteCount = 1;
        }

        // 如果是根评论，级联删除所有子评论
        if (comment.getRootId() == null) {
            // 查询并删除所有子评论
            LambdaQueryWrapper<BlogComment> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(BlogComment::getRootId, commentId)
                    .eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode());
            Long approvedChildCount = commentMapper.selectCount(childWrapper);
            deleteCount += approvedChildCount.intValue();

            // 删除子评论
            LambdaUpdateWrapper<BlogComment> deleteChildWrapper = new LambdaUpdateWrapper<>();
            deleteChildWrapper.eq(BlogComment::getRootId, commentId);
            commentMapper.delete(deleteChildWrapper);
        }

        // 删除当前评论
        commentMapper.deleteById(commentId);

        // 更新文章评论数
        if (deleteCount > 0) {
            BlogArticle article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                article.setCommentCount(Math.max(0, article.getCommentCount() - deleteCount));
                articleMapper.updateById(article);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditComments(List<Long> commentIds, Integer auditStatus) {
        if (CollectionUtils.isEmpty(commentIds)) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_IDS_EMPTY);
        }

        // 验证审核状态
        if (!AuditStatusEnum.isApproved(auditStatus) && !AuditStatusEnum.isRejected(auditStatus)) {
            throw new BusinessException(CommentConstants.ERR_INVALID_AUDIT_STATUS);
        }

        for (Long commentId : commentIds) {
            auditComment(commentId, auditStatus);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> commentIds) {
        if (CollectionUtils.isEmpty(commentIds)) {
            throw new BusinessException(CommentConstants.ERR_COMMENT_IDS_EMPTY);
        }

        for (Long commentId : commentIds) {
            deleteComment(commentId);
        }
    }

    @Override
    public Long getPendingAuditCount() {
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getAuditStatus, AuditStatusEnum.PENDING.getCode());
        return commentMapper.selectCount(wrapper);
    }

    /**
     * 将评论实体转换为管理端VO
     */
    private CommentAdminVO convertToAdminVO(BlogComment comment) {
        CommentAdminVO vo = new CommentAdminVO();
        BeanUtils.copyProperties(comment, vo);

        // 获取文章标题
        BlogArticle article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            vo.setArticleTitle(article.getTitle());
        }

        // 获取评论用户信息
        SysUser user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        // 获取被回复用户信息
        if (comment.getReplyUserId() != null) {
            SysUser replyUser = userMapper.selectById(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyNickname(replyUser.getNickname());
            }
        }

        // 设置审核状态描述
        AuditStatusEnum statusEnum = AuditStatusEnum.fromCode(comment.getAuditStatus());
        if (statusEnum != null) {
            vo.setAuditStatusDesc(statusEnum.getDesc());
        }

        return vo;
    }
}
