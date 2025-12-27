package com.nebula.boot4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.boot4.entity.SysUser;
import com.nebula.boot4.mapper.SysUserMapper;
import com.nebula.boot4.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public Page<SysUser> pageUsers(Long current, Long size, String username) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果提供了用户名，进行模糊查询
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return this.getOne(queryWrapper);
    }
}
