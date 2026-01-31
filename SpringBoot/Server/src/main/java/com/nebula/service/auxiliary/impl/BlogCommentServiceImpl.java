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
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCommentMapper;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.auxiliary.BlogCommentService;
import com.nebula.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogArticleMapper articleMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(CommentDTO commentDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        BlogComment comment = new BlogComment();
        BeanUtils.copyProperties(commentDTO, comment);
        comment.setUserId(userId);
        comment.setLikeCount(CountConstants.INIT_VALUE);
        comment.setAuditStatus(AuditStatusEnum.APPROVED.getCode());
        commentMapper.insert(comment);

        BlogArticle article = articleMapper.selectById(commentDTO.getArticleId());
        if (article != null) {
            article.setCommentCount(article.getCommentCount() + CountConstants.INCREMENT);
            articleMapper.updateById(article);
        }

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        BlogComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }
        commentMapper.deleteById(id);

        BlogArticle article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            article.setCommentCount(Math.max(CountConstants.INIT_VALUE, article.getCommentCount() - CountConstants.INCREMENT));
            articleMapper.updateById(article);
        }
    }

    @Override
    public void likeComment(Long commentId) {
        // 简化实现,直接更新点赞数
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + CountConstants.INCREMENT);
            commentMapper.updateById(comment);
        }
    }

    @Override
    public Page<CommentVO> getArticleComments(Long articleId, Long current, Long size) {
        Page<BlogComment> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getArticleId, articleId);
        wrapper.isNull(BlogComment::getParentId);
        wrapper.eq(BlogComment::getAuditStatus, AuditStatusEnum.APPROVED.getCode());
        wrapper.orderByDesc(BlogComment::getCreateTime);

        Page<BlogComment> commentPage = commentMapper.selectPage(page, wrapper);

        Page<CommentVO> voPage = new Page<>(current, size);
        voPage.setTotal(commentPage.getTotal());
        List<CommentVO> voList = commentPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        for (CommentVO vo : voList) {
            LambdaQueryWrapper<BlogComment> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(BlogComment::getParentId, vo.getId());
            childWrapper.eq(BlogComment::getAuditStatus, 1);
            childWrapper.orderByAsc(BlogComment::getCreateTime);
            List<BlogComment> children = commentMapper.selectList(childWrapper);
            vo.setChildren(children.stream().map(this::convertToVO).collect(Collectors.toList()));
        }

        voPage.setRecords(voList);
        return voPage;
    }

    private CommentVO convertToVO(BlogComment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        SysUser user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
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
