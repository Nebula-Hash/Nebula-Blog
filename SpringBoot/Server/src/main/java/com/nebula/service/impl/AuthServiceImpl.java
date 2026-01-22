package com.nebula.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.LoginDTO;
import com.nebula.dto.RegisterDTO;
import com.nebula.entity.SysUser;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.AuthService;
import com.nebula.utils.MD5Utils;
import com.nebula.vo.LoginVO;
import com.nebula.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(queryWrapper);

        // 2. 校验用户
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验密码（使用MD5加密）
        String encryptPassword = MD5Utils.md5(loginDTO.getPassword());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 5. 登录成功，生成 Token
        StpUtil.login(user.getId());

        // 6. 返回登录信息
        return buildLoginVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 1. 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 2. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, registerDTO.getUsername());
        Long count = sysUserMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 检查邮箱是否已存在（如果提供了邮箱）
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getEmail, registerDTO.getEmail());
            count = sysUserMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 4. 检查手机号是否已存在（如果提供了手机号）
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, registerDTO.getPhone());
            count = sysUserMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 5. 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword((registerDTO.getPassword())); // MD5加密
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setStatus(1); // 默认启用

        // 6. 保存用户
        int result = sysUserMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("注册失败");
        }

        // 7. 自动登录
        StpUtil.login(user.getId());

        // 8. 返回登录信息
        return buildLoginVO(user);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getCurrentUser() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询用户信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 转换为 VO
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    public LoginVO refreshToken() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询用户信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 刷新 Token（延长有效期）
        StpUtil.renewTimeout(StpUtil.getTokenTimeout());

        // 返回新的登录信息
        return buildLoginVO(user);
    }

    /**
     * 构建登录返回对象
     */
    private LoginVO buildLoginVO(SysUser user) {
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setToken(StpUtil.getTokenValue());
        loginVO.setTokenName(StpUtil.getTokenName());
        loginVO.setTokenTimeout(StpUtil.getTokenTimeout());
        return loginVO;
    }
}
