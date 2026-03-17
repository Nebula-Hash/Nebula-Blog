package com.nebula.constant;

/**
 * 认证相关常量
 * <p>
 * 统一管理认证、登录保护、注册限制等相关常量
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class AuthConstants {

    private AuthConstants() {
        // 私有构造函数，防止实例化
    }

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
     * 最大登录失败次数
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 账号锁定时间（分钟）
     */
    public static final int LOCK_TIME_MINUTES = 30;

    // ======================== 注册限制常量 ========================

    /**
     * 每小时最大注册次数（同IP）
     */
    public static final int MAX_REGISTER_PER_HOUR = 5;

    /**
     * 注册限制时间（小时）
     */
    public static final int REGISTER_LIMIT_HOURS = 1;
}
