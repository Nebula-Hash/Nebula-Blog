package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogArticle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogArticleMapper extends BaseMapper<BlogArticle> {
}
