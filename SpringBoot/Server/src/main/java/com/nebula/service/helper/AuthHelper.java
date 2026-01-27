package com.nebula.service.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.RegisterDTO;
import com.nebula.entity.SysRole;
import com.nebula.entity.SysUser;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.SysRoleMapper;
import com.nebula.mapper.SysUserMapper;
import com.nebula.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private final SysRoleMapper sysRoleMapper;

    /**
     * 管理员角色标识
     */
    public static final String ADMIN_ROLE_KEY = "admin";

    /**
     * 普通用户角色标识
     */
    public static final String USER_ROLE_KEY = "user";

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
     * 查询用户角色Key
     *
     * @param roleId 角色ID
     * @return 角色Key，不存在返回null
     */
    public String getRoleKey(Long roleId) {
        if (roleId == null) {
            return null;
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        return role != null ? role.getRoleKey() : null;
    }

    /**
     * 获取普通用户角色ID
     *
     * @return 角色ID，不存在返回null
     */
    public Long getUserRoleId() {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getRoleKey, USER_ROLE_KEY);
        queryWrapper.eq(SysRole::getStatus, 1);
        SysRole role = sysRoleMapper.selectOne(queryWrapper);
        return role != null ? role.getId() : null;
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
     * @return 角色对象
     * @throws BusinessException 如果不是管理员
     */
    public SysRole checkAdminRole(SysUser user) {
        if (user.getRoleId() == null) {
            throw new BusinessException("您没有管理后台访问权限");
        }
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        if (role == null || !ADMIN_ROLE_KEY.equals(role.getRoleKey())) {
            throw new BusinessException("您没有管理后台访问权限");
        }
        return role;
    }
}
