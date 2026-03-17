package com.nebula.constant;

/**
 * HTTP 状态码常量
 * <p>
 * 统一管理 HTTP 状态码，避免魔法数字
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class HttpConstants {

    private HttpConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * HTTP 401 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * HTTP 403 禁止访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * HTTP 404 资源不存在
     */
    public static final int NOT_FOUND = 404;

    /**
     * HTTP 500 服务器内部错误
     */
    public static final int INTERNAL_ERROR = 500;
}
