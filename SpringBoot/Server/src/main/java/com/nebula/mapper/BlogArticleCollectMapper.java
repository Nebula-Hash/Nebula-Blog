package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogArticleCollect;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章收藏Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogArticleCollectMapper extends BaseMapper<BlogArticleCollect> {
}
