package com.nebula.boot4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.boot4.entity.BlogArticleLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章点赞Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogArticleLikeMapper extends BaseMapper<BlogArticleLike> {
}
