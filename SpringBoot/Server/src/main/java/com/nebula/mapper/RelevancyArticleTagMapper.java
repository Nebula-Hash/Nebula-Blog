package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.RelevancyArticleTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章标签关联Mapper接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Mapper
public interface RelevancyArticleTagMapper extends BaseMapper<RelevancyArticleTag> {
}
