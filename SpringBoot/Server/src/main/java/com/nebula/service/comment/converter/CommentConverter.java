package com.nebula.service.comment.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogComment;
import com.nebula.entity.SysUser;
import com.nebula.enumeration.AuditStatusEnum;
import com.nebula.service.comment.helper.CommentQueryHelper;
import com.nebula.vo.CommentAdminVO;
import com.nebula.vo.CommentClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论VO转换器
 * <p>
 * 封装评论实体与VO对象之间的转换逻辑
 *
 * @author Nebula-Hash
 * @date 2026/2/1
 */
@Component
@RequiredArgsConstructor
public class CommentConverter {

    private final CommentQueryHelper queryHelper;

    // ==================== 客户端转换方法 ====================

    /**
     * 批量转换为客户端VO（楼中楼结构）
     * <p>
     * 将根评论和子评论组装成树形结构
     *
     * @param rootComments 根评论列表
     * @param allReplies   所有回复列表
     * @return 客户端评论VO列表
     */
    public List<CommentClientVO> batchToClientVO(List<BlogComment> rootComments, List<BlogComment> allReplies) {
        if (rootComments == null || rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 合并所有评论，批量获取用户信息
        List<BlogComment> allComments = new ArrayList<>(rootComments);
        if (allReplies != null) {
            allComments.addAll(allReplies);
        }

        Map<Long, SysUser> userMap = queryHelper.batchGetUserMap(allComments);
        Set<Long> likedCommentIds = queryHelper.getCurrentUserLikedCommentIds(allComments);

        // 按 rootId 分组回复
        Map<Long, List<BlogComment>> replyMap = (allReplies == null) ? Collections.emptyMap() :
                allReplies.stream().collect(Collectors.groupingBy(BlogComment::getRootId));

        // 组装结果
        List<CommentClientVO> voList = new ArrayList<>();
        for (BlogComment rootComment : rootComments) {
            CommentClientVO rootVO = toClientVO(rootComment, userMap, likedCommentIds);

            // 获取该根评论下的所有回复
            List<BlogComment> replies = replyMap.getOrDefault(rootComment.getId(), Collections.emptyList());
            List<CommentClientVO> childrenVOs = replies.stream()
                    .map(reply -> toClientVO(reply, userMap, likedCommentIds))
                    .collect(Collectors.toList());
            rootVO.setChildren(childrenVOs);

            voList.add(rootVO);
        }

        return voList;
    }

    /**
     * 转换单个评论为客户端VO
     *
     * @param comment         评论实体
     * @param userMap         用户信息Map
     * @param likedCommentIds 已点赞的评论ID集合
     * @return 客户端评论VO
     */
    public CommentClientVO toClientVO(BlogComment comment, Map<Long, SysUser> userMap, Set<Long> likedCommentIds) {
        CommentClientVO vo = new CommentClientVO();
        BeanUtils.copyProperties(comment, vo);

        // 设置评论者信息
        SysUser user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        // 设置被回复者信息
        if (comment.getReplyUserId() != null) {
            SysUser replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyNickname(replyUser.getNickname());
            }
        }

        // 设置点赞状态
        vo.setIsLiked(likedCommentIds != null && likedCommentIds.contains(comment.getId()));

        return vo;
    }

    /**
     * 转换客户端评论分页结果
     *
     * @param rootPage     根评论分页数据
     * @param rootComments 根评论列表
     * @param allReplies   所有回复列表
     * @param current      当前页
     * @param size         每页大小
     * @return 客户端评论VO分页
     */
    public Page<CommentClientVO> toClientVOPage(Page<BlogComment> rootPage, List<BlogComment> rootComments,
                                                 List<BlogComment> allReplies, Long current, Long size) {
        Page<CommentClientVO> voPage = new Page<>(current, size);
        voPage.setTotal(rootPage.getTotal());
        voPage.setRecords(batchToClientVO(rootComments, allReplies));
        return voPage;
    }

    // ==================== 管理端转换方法 ====================

    /**
     * 批量转换为管理端VO
     *
     * @param comments 评论列表
     * @return 管理端评论VO列表
     */
    public List<CommentAdminVO> batchToAdminVO(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, SysUser> userMap = queryHelper.batchGetUserMap(comments);
        Map<Long, BlogArticle> articleMap = queryHelper.batchGetArticleMap(comments);

        return comments.stream()
                .map(comment -> toAdminVO(comment, userMap, articleMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换单个评论为管理端VO
     *
     * @param comment    评论实体
     * @param userMap    用户信息Map
     * @param articleMap 文章信息Map
     * @return 管理端评论VO
     */
    public CommentAdminVO toAdminVO(BlogComment comment, Map<Long, SysUser> userMap, Map<Long, BlogArticle> articleMap) {
        CommentAdminVO vo = new CommentAdminVO();
        BeanUtils.copyProperties(comment, vo);

        // 设置文章标题
        BlogArticle article = articleMap.get(comment.getArticleId());
        if (article != null) {
            vo.setArticleTitle(article.getTitle());
        }

        // 设置评论者信息
        SysUser user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        // 设置被回复者信息
        if (comment.getReplyUserId() != null) {
            SysUser replyUser = userMap.get(comment.getReplyUserId());
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

    /**
     * 转换管理端评论分页结果
     *
     * @param commentPage 评论分页数据
     * @param current     当前页
     * @param size        每页大小
     * @return 管理端评论VO分页
     */
    public Page<CommentAdminVO> toAdminVOPage(Page<BlogComment> commentPage, Long current, Long size) {
        Page<CommentAdminVO> voPage = new Page<>(current, size);
        voPage.setTotal(commentPage.getTotal());
        voPage.setRecords(batchToAdminVO(commentPage.getRecords()));
        return voPage;
    }
}
