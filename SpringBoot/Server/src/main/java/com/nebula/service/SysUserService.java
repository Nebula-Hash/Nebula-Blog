package com.nebula.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.entity.SysUser;

/**
 * 用户服务接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     *
     * @param current 当前页
     * @param size    每页大小
     * @param username 用户名（可选）
     * @return 分页结果
     */
    Page<SysUser> pageUsers(Long current, Long size, String username);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);
}
