package com.nebula.service.security;

/**
 * 登录防护服务接口
 * <p>
 * 提供登录安全相关功能：账号锁定、失败记录等
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
public interface LoginProtectionService {

    /**
     * 检查账号是否被锁定
     *
     * @param username 用户名
     * @throws com.nebula.exception.BusinessException 如果账号被锁定
     */
    void checkAccountLocked(String username);

    /**
     * 记录登录失败
     *
     * @param username 用户名
     */
    void recordLoginFail(String username);

    /**
     * 清除登录失败记录（登录成功后调用）
     *
     * @param username 用户名
     */
    void clearLoginFail(String username);
}
