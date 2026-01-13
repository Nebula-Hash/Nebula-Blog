package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface BlogMessageMapper extends BaseMapper<BlogMessage> {
}
