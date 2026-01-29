package com.nebula.service.authority.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.nebula.dto.LoginDTO;
import com.nebula.dto.RegisterDTO;
import com.nebula.entity.SysUser;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.authority.AuthService;
import com.nebula.service.authority.helper.AuthHelper;
import com.nebula.service.authority.security.LoginProtectionService;
import com.nebula.service.authority.security.RegisterLimitService;
import com.nebula.utils.PasswordUtils;
import com.nebula.vo.LoginVO;
import com.nebula.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final LoginProtectionService loginProtectionService;
    private final RegisterLimitService registerLimitService;
    private final AuthHelper authHelper;

    @Override
    public LoginVO clientLogin(LoginDTO loginDTO) {
        return doLogin(loginDTO, false);
    }

    @Override
    public LoginVO adminLogin(LoginDTO loginDTO) {
        return doLogin(loginDTO, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 1. 检查注册频率限制
        String clientIp = AuthHelper.getClientIp();
        registerLimitService.checkRegisterLimit(clientIp);

        // 2. 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 3. 检查用户名/邮箱/手机号是否已存在（合并为一次查询）
        authHelper.checkUserExists(registerDTO);

        // 4. 创建用户
        SysUser user = buildNewUser(registerDTO);

        // 5. 保存用户（捕获唯一键冲突异常）
        saveUserWithDuplicateCheck(user);

        // 6. 记录注册次数
        registerLimitService.recordRegister(clientIp);

        // 7. 自动登录
        StpUtil.login(user.getId());
        StpUtil.getSession().set("roleKey", AuthHelper.USER_ROLE_KEY);

        return authHelper.buildLoginVO(user, AuthHelper.USER_ROLE_KEY);
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        userInfoVO.setRoleKey(user.getRoleKey());
        return userInfoVO;
    }

    @Override
    public LoginVO clientRefreshToken() {
        return doRefreshToken(false);
    }

    @Override
    public LoginVO adminRefreshToken() {
        return doRefreshToken(true);
    }

    // ======================== 私有方法 ========================

    /**
     * 统一登录处理
     *
     * @param loginDTO     登录参数
     * @param requireAdmin 是否需要管理员角色
     * @return 登录结果
     */
    private LoginVO doLogin(LoginDTO loginDTO, boolean requireAdmin) {
        String username = loginDTO.getUsername();

        // 1. 检查账号是否被锁定
        loginProtectionService.checkAccountLocked(username);

        // 2. 查询用户
        SysUser user = authHelper.findByUsername(username);
        if (user == null) {
            loginProtectionService.recordLoginFail(username);
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验密码
        if (!PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            loginProtectionService.recordLoginFail(username);
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 校验用户状态
        authHelper.checkUserStatus(user);

        // 5. 校验管理员角色（仅管理端）
        if (requireAdmin) {
            authHelper.checkAdminRole(user);
        }

        // 6. 登录成功处理
        loginProtectionService.clearLoginFail(username);
        String roleKey = user.getRoleKey();

        StpUtil.login(user.getId());
        if (roleKey != null) {
            StpUtil.getSession().set("roleKey", roleKey);
        }

        return authHelper.buildLoginVO(user, roleKey);
    }

    /**
     * 统一刷新 Token 处理
     *
     * @param requireAdmin 是否需要管理员角色
     * @return 登录结果
     */
    private LoginVO doRefreshToken(boolean requireAdmin) {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(401, "Token已失效，请重新登录");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        authHelper.checkUserStatus(user);

        if (requireAdmin) {
            authHelper.checkAdminRole(user);
        }

        StpUtil.renewTimeout(AuthHelper.TOKEN_TIMEOUT_SECONDS);
        StpUtil.getSession().set("roleKey", user.getRoleKey());

        return authHelper.buildLoginVO(user, user.getRoleKey());
    }

    /**
     * 构建新用户对象
     */
    private SysUser buildNewUser(RegisterDTO registerDTO) {
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtils.encode(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname()) 
                ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(StringUtils.hasText(registerDTO.getEmail()) ? registerDTO.getEmail() : null);
        user.setRoleKey(AuthHelper.USER_ROLE_KEY);
        user.setStatus(1);
        return user;
    }

    /**
     * 保存用户（带唯一键冲突检查）
     */
    private void saveUserWithDuplicateCheck(SysUser user) {
        try {
            int result = sysUserMapper.insert(user);
            if (result <= 0) {
                throw new BusinessException("注册失败");
            }
        } catch (DuplicateKeyException e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("username")) {
                    throw new BusinessException("用户名已存在");
                } else if (message.contains("email")) {
                    throw new BusinessException("邮箱已被注册");
                }
            }
            throw new BusinessException("用户名或邮箱已被注册");
        }
    }
}
