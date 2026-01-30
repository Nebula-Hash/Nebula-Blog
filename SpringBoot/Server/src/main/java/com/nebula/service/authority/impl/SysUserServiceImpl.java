package com.nebula.service.authority.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.dto.UserDTO;
import com.nebula.entity.SysUser;
import com.nebula.enumeration.StatusEnum;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.authority.SysUserService;
import com.nebula.utils.PasswordUtils;
import com.nebula.vo.UserAdminVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public Page<UserAdminVO> pageUsersByRole(Long current, Long size, String roleKey) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据角色标识筛选
        queryWrapper.eq(SysUser::getRoleKey, roleKey);
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> userPage = this.page(page, queryWrapper);
        
        // 转换为VO
        Page<UserAdminVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserAdminVO> voList = userPage.getRecords().stream().map(this::convertToVO).toList();
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public UserAdminVO getUserById(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return this.getOne(queryWrapper);
    }

    @Override
    public SysUser getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail, email);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean addUser(UserDTO userDTO, String roleKey) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 设置角色标识
        user.setRoleKey(roleKey);
        // 加密密码
        user.setPassword(PasswordUtils.encode(userDTO.getPassword()));
        // 默认启用状态
        if (user.getStatus() == null) {
            user.setStatus(StatusEnum.ENABLED.getCode());
        }
        // 处理可选唯一字段
        normalizeOptionalFields(user);
        
        return this.save(user);
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 用户名不允许修改
        user.setUsername(null);
        
        // 如果密码不为空，则加密更新
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(PasswordUtils.encode(userDTO.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        // 处理可选唯一字段
        normalizeOptionalFields(user);
        
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }

    @Override
    public Page<UserAdminVO> searchUsers(Long current, Long size, String keyword, Integer status) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        // 多字段模糊匹配（用户名或昵称）
        queryWrapper.and(wrapper -> wrapper
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getNickname, keyword)
        );
        
        // 状态筛选（可选）
        if (status != null) {
            queryWrapper.eq(SysUser::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> userPage = this.page(page, queryWrapper);
        
        // 转换为VO
        Page<UserAdminVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserAdminVO> voList = userPage.getRecords().stream().map(this::convertToVO).toList();
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 实体转换为VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserAdminVO convertToVO(SysUser user) {
        UserAdminVO vo = new UserAdminVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 规范化可选唯一字段，将空字符串转为null避免数据库唯一索引冲突
     *
     * @param user 用户实体
     */
    private void normalizeOptionalFields(SysUser user) {
        if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
            user.setEmail(null);
        }
    }
}
