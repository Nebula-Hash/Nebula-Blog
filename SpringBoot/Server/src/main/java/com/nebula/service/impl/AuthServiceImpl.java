package com.nebula.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.nebula.dto.LoginDTO;
import com.nebula.dto.RegisterDTO;
import com.nebula.entity.SysRole;
import com.nebula.entity.SysUser;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.AuthService;
import com.nebula.service.helper.AuthHelper;
import com.nebula.service.security.LoginProtectionService;
import com.nebula.service.security.RegisterLimitService;
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
    public LoginVO login(LoginDTO loginDTO) {
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

        // 5. 登录成功处理
        loginProtectionService.clearLoginFail(username);
        String roleKey = authHelper.getRoleKey(user.getRoleId());

        StpUtil.login(user.getId());
        if (roleKey != null) {
            StpUtil.getSession().set("roleKey", roleKey);
        }

        return authHelper.buildLoginVO(user, roleKey);
    }

    @Override
    public LoginVO adminLogin(LoginDTO loginDTO) {
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

        // 5. 校验管理员角色
        SysRole role = authHelper.checkAdminRole(user);

        // 6. 登录成功处理
        loginProtectionService.clearLoginFail(username);

        StpUtil.login(user.getId());
        StpUtil.getSession().set("roleKey", role.getRoleKey());

        return authHelper.buildLoginVO(user, role.getRoleKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 1. 检查注册频率限制
        String clientIp = registerLimitService.getClientIp();
        registerLimitService.checkRegisterLimit(clientIp);

        // 2. 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 3. 检查用户名/邮箱/手机号是否已存在（合并为一次查询）
        authHelper.checkUserExists(registerDTO);

        // 4. 查询普通用户角色ID
        Long userRoleId = authHelper.getUserRoleId();

        // 5. 创建用户
        SysUser user = buildNewUser(registerDTO, userRoleId);

        // 6. 保存用户（捕获唯一键冲突异常）
        saveUserWithDuplicateCheck(user);

        // 7. 记录注册次数
        registerLimitService.recordRegister(clientIp);

        // 8. 自动登录
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

        String roleKey = authHelper.getRoleKey(user.getRoleId());

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        userInfoVO.setRoleKey(roleKey);
        return userInfoVO;
    }

    @Override
    public LoginVO refreshToken() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(401, "Token已失效，请重新登录");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        authHelper.checkUserStatus(user);
        String roleKey = authHelper.getRoleKey(user.getRoleId());

        StpUtil.renewTimeout(86400);

        return authHelper.buildLoginVO(user, roleKey);
    }

    @Override
    public LoginVO adminRefreshToken() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(401, "Token已失效，请重新登录");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        authHelper.checkUserStatus(user);
        SysRole role = authHelper.checkAdminRole(user);

        StpUtil.renewTimeout(86400);
        StpUtil.getSession().set("roleKey", role.getRoleKey());

        return authHelper.buildLoginVO(user, role.getRoleKey());
    }

    // ======================== 私有方法 ========================

    /**
     * 构建新用户对象
     */
    private SysUser buildNewUser(RegisterDTO registerDTO, Long userRoleId) {
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtils.encode(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname()) 
                ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(StringUtils.hasText(registerDTO.getEmail()) ? registerDTO.getEmail() : null);
        user.setPhone(StringUtils.hasText(registerDTO.getPhone()) ? registerDTO.getPhone() : null);
        user.setRoleId(userRoleId);
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
                } else if (message.contains("phone")) {
                    throw new BusinessException("手机号已被注册");
                }
            }
            throw new BusinessException("用户名、邮箱或手机号已被注册");
        }
    }
}
