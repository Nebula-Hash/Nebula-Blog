package com.nebula.boot4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.boot4.entity.BlogTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogTagMapper extends BaseMapper<BlogTag> {
}
