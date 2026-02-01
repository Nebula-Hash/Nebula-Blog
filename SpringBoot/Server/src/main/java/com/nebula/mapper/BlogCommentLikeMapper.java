package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogCommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞Mapper接口
 *
 * @author Nebula-Hash
 * @date 2026/2/1
 */
@Mapper
public interface BlogCommentLikeMapper extends BaseMapper<BlogCommentLike> {
}
