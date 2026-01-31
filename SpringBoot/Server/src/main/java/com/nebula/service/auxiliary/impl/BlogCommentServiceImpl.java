package com.nebula.service.auxiliary.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.CountConstants;
import com.nebula.dto.CommentDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogComment;
import com.nebula.entity.SysUser;
import com.nebula.enumeration.AuditStatusEnum;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCommentMapper;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.auxiliary.BlogCommentService;
import com.nebula.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<CommentVO> getArticleComments(Long articleId, Long current, Long size) {
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
            Page<CommentVO> emptyPage = new Page<>(current, size);
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
        List<CommentVO> voList = new ArrayList<>();
        for (BlogComment rootComment : rootComments) {
            CommentVO rootVO = convertToVO(rootComment);
            
            // 获取该根评论下的所有回复，作为 children 扁平展示
            List<BlogComment> replies = replyMap.getOrDefault(rootComment.getId(), Collections.emptyList());
            List<CommentVO> childrenVOs = replies.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            rootVO.setChildren(childrenVOs);
            
            voList.add(rootVO);
        }

        Page<CommentVO> voPage = new Page<>(current, size);
        voPage.setTotal(rootPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 将评论实体转换为VO
     */
    private CommentVO convertToVO(BlogComment comment) {
        CommentVO vo = new CommentVO();
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
}
