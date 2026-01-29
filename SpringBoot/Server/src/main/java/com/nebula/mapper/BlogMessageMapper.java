package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Mapper
public interface BlogMessageMapper extends BaseMapper<SysNotification> {
}
