package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogViewHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 浏览记录Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogViewHistoryMapper extends BaseMapper<BlogViewHistory> {
}
