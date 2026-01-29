package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.dto.UserDTO;
import com.nebula.entity.SysUser;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.SysUserService;
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
    public boolean addUser(UserDTO userDTO, String roleKey) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 设置角色标识
        user.setRoleKey(roleKey);
        // 加密密码
        user.setPassword(PasswordUtils.encode(userDTO.getPassword()));
        // 默认启用状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        return this.save(user);
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 如果密码不为空，则加密更新
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(PasswordUtils.encode(userDTO.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
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
}
