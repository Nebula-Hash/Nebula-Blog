package com.nebula.boot4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.boot4.entity.BlogArticle;
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
