package com.nebula.service.authority.security;

/**
 * 注册限流服务接口
 * <p>
 * 提供注册防刷功能：IP限频等
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
public interface RegisterLimitService {

    /**
     * 检查注册频率限制
     *
     * @param ip 客户端IP
     * @throws com.nebula.exception.BusinessException 如果注册过于频繁
     */
    void checkRegisterLimit(String ip);

    /**
     * 记录注册次数（注册成功后调用）
     *
     * @param ip 客户端IP
     */
    void recordRegister(String ip);
}
