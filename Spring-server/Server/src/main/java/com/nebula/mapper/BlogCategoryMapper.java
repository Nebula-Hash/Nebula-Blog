package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Mapper
public interface BlogCategoryMapper extends BaseMapper<BlogCategory> {
}
