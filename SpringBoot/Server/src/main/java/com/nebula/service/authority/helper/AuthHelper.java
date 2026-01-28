package com.nebula.service.authority.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.RegisterDTO;
import com.nebula.entity.SysUser;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.SysUserMapper;
import com.nebula.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 认证辅助类
 * <p>
 * 提供认证相关的通用方法：构建VO、查询角色、检查用户存在等
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final SysUserMapper sysUserMapper;

    // ======================== 角色常量 ========================

    /**
     * 管理员角色标识
     */
    public static final String ADMIN_ROLE_KEY = "admin";

    /**
     * 普通用户角色标识
     */
    public static final String USER_ROLE_KEY = "user";

    // ======================== Token 常量 ========================

    /**
     * Token 过期时间（秒）- 24小时
     */
    public static final long TOKEN_TIMEOUT_SECONDS = 86400L;

    // ======================== 登录保护常量 ========================

    /**
     * 登录失败记录 Redis Key 前缀
     */
    public static final String LOGIN_FAIL_KEY_PREFIX = "nebula:login:fail:";

    /**
     * 最大登录失败次数
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 账号锁定时间（分钟）
     */
    public static final int LOCK_TIME_MINUTES = 30;

    // ======================== 注册限制常量 ========================

    /**
     * 注册限制 Redis Key 前缀
     */
    public static final String REGISTER_LIMIT_KEY_PREFIX = "nebula:register:limit:";

    /**
     * 每小时最大注册次数（同IP）
     */
    public static final int MAX_REGISTER_PER_HOUR = 5;

    /**
     * 注册限制时间（小时）
     */
    public static final int REGISTER_LIMIT_HOURS = 1;

    /**
     * 构建登录返回对象
     *
     * @param user    用户信息
     * @param roleKey 角色标识
     * @return 登录返回对象
     */
    public LoginVO buildLoginVO(SysUser user, String roleKey) {
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setRoleKey(roleKey);
        loginVO.setToken(StpUtil.getTokenValue());
        loginVO.setTokenName(StpUtil.getTokenName());
        loginVO.setTokenTimeout(StpUtil.getTokenTimeout());
        return loginVO;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    public SysUser findByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(queryWrapper);
    }

    /**
     * 检查用户名/邮箱/手机号是否已存在（合并为一次查询）
     *
     * @param registerDTO 注册参数
     * @throws BusinessException 如果存在重复
     */
    public void checkUserExists(RegisterDTO registerDTO) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 用户名必须检查
        queryWrapper.eq(SysUser::getUsername, registerDTO.getUsername());

        // 邮箱可选检查
        if (StringUtils.hasText(registerDTO.getEmail())) {
            queryWrapper.or().eq(SysUser::getEmail, registerDTO.getEmail());
        }

        // 手机号可选检查
        if (StringUtils.hasText(registerDTO.getPhone())) {
            queryWrapper.or().eq(SysUser::getPhone, registerDTO.getPhone());
        }

        // 查询存在的用户
        List<SysUser> existUsers = sysUserMapper.selectList(queryWrapper);

        if (!existUsers.isEmpty()) {
            for (SysUser existUser : existUsers) {
                if (existUser.getUsername().equals(registerDTO.getUsername())) {
                    throw new BusinessException("用户名已存在");
                }
                if (StringUtils.hasText(registerDTO.getEmail())
                        && registerDTO.getEmail().equals(existUser.getEmail())) {
                    throw new BusinessException("邮箱已被注册");
                }
                if (StringUtils.hasText(registerDTO.getPhone())
                        && registerDTO.getPhone().equals(existUser.getPhone())) {
                    throw new BusinessException("手机号已被注册");
                }
            }
        }
    }

    /**
     * 校验用户状态
     *
     * @param user 用户对象
     * @throws BusinessException 如果用户被禁用
     */
    public void checkUserStatus(SysUser user) {
        // 使用 Integer.valueOf(0).equals() 避免 NPE
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
    }

    /**
     * 校验管理员角色
     *
     * @param user 用户对象
     * @throws BusinessException 如果不是管理员
     */
    public void checkAdminRole(SysUser user) {
        if (!ADMIN_ROLE_KEY.equals(user.getRoleKey())) {
            throw new BusinessException("您没有管理后台访问权限");
        }
    }

    // ======================== 工具方法 ========================

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP
     */
    public static String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();

        // 优先从 X-Forwarded-For 获取（适用于反向代理场景）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}
