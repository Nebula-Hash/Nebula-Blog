package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {
}
