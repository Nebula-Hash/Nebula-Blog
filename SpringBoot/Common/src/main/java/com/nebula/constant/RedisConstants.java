package com.nebula.constant;

/**
 * Redis Key 常量
 * <p>
 * 统一管理 Redis Key 前缀，避免硬编码
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class RedisConstants {

    private RedisConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * Redis Key 通用前缀
     */
    public static final String PREFIX = "nebula:";

    /**
     * 登录失败记录 Redis Key 前缀
     */
    public static final String LOGIN_FAIL = PREFIX + "login:fail:";

    /**
     * 注册限制 Redis Key 前缀
     */
    public static final String REGISTER_LIMIT = PREFIX + "register:limit:";

    /**
     * 访问总量 Redis Key
     */
    public static final String VISIT_TOTAL = PREFIX + "visit:total";

    /**
     * 每日访问量 Redis Key 前缀
     */
    public static final String VISIT_DAILY = PREFIX + "visit:daily:";
}
