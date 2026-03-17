package com.nebula.constant;

/**
 * 通用常量
 * <p>
 * 统一管理通用的常量值
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class CommonConstants {

    private CommonConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * 未知IP
     */
    public static final String UNKNOWN_IP = "unknown";

    // ==================== 分页默认值 ====================

    /**
     * 默认当前页
     */
    public static final String DEFAULT_PAGE_CURRENT = "1";

    /**
     * 默认每页大小
     */
    public static final String DEFAULT_PAGE_SIZE = "10";

    // ==================== 通用消息 ====================

    /**
     * 操作成功
     */
    public static final String MSG_SUCCESS = "操作成功";
}
