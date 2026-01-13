package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
