package com.nebula.service.authority;

import com.nebula.dto.LoginDTO;
import com.nebula.dto.RegisterDTO;
import com.nebula.vo.LoginVO;
import com.nebula.vo.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface AuthService {

    /**
     * 客户端用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginVO clientLogin(LoginDTO loginDTO);

    /**
     * 管理端用户登录（需校验管理员角色）
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginVO adminLogin(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 注册结果
     */
    LoginVO register(RegisterDTO registerDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getCurrentUser();

    /**
     * 客户端刷新 Token
     *
     * @return 新的 Token 信息
     */
    LoginVO clientRefreshToken();

    /**
     * 管理端刷新 Token（需校验管理员角色）
     *
     * @return 新的 Token 信息
     */
    LoginVO adminRefreshToken();
}
