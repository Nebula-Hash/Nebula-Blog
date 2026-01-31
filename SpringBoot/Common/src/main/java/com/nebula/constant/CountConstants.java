package com.nebula.constant;

/**
 * 计数相关常量
 * <p>
 * 统一管理计数初始值、增量等相关常量
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class CountConstants {

    private CountConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * 计数初始值
     */
    public static final int INIT_VALUE = 0;

    /**
     * 计数增量
     */
    public static final int INCREMENT = 1;

    /**
     * 每日访问记录过期天数
     */
    public static final int VISIT_DAILY_EXPIRE_DAYS = 2;
}
