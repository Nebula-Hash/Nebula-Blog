/**
 * 全局常量配置
 * 统一管理所有魔法数字和硬编码配置
 */

// ==================== Token 配置 ====================
export const TOKEN_CONFIG = {
    // Token 刷新阈值（5分钟）
    REFRESH_THRESHOLD: 5 * 60 * 1000,
    // Token 刷新超时时间（10秒）
    REFRESH_TIMEOUT: 10000,
    // 请求队列最大大小
    MAX_QUEUE_SIZE: 50
}

// ==================== 缓存配置 ====================
export const CACHE_CONFIG = {
    // 用户信息缓存时长（5分钟）
    USER_INFO_TTL: 5 * 60 * 1000,
    // 错误消息缓存时长（1秒）
    ERROR_MESSAGE_TTL: 1000
}

// ==================== 消息提示配置 ====================
export const MESSAGE_CONFIG = {
    // 成功消息显示时长（3秒）
    SUCCESS_DURATION: 3000,
    // 错误消息显示时长（5秒）
    ERROR_DURATION: 5000,
    // 警告消息显示时长（4秒）
    WARNING_DURATION: 4000,
    // 信息消息显示时长（3秒）
    INFO_DURATION: 3000
}

// ==================== 分页配置 ====================
export const PAGINATION_CONFIG = {
    // 默认每页大小
    DEFAULT_PAGE_SIZE: 10,
    // 热门文章数量
    HOT_ARTICLES_SIZE: 5,
    // 推荐文章数量
    RECOMMEND_ARTICLES_SIZE: 3,
    // 评论列表每页大小
    COMMENT_PAGE_SIZE: 100
}

// ==================== 表单验证配置 ====================
export const VALIDATION_CONFIG = {
    // 用户名长度限制
    USERNAME_MIN_LENGTH: 3,
    USERNAME_MAX_LENGTH: 20,
    // 密码长度限制
    PASSWORD_MIN_LENGTH: 6,
    PASSWORD_MAX_LENGTH: 20,
    // 昵称长度限制
    NICKNAME_MAX_LENGTH: 50,
    // 评论内容长度限制
    COMMENT_MIN_LENGTH: 1,
    COMMENT_MAX_LENGTH: 500
}

// ==================== HTTP 配置 ====================
export const HTTP_CONFIG = {
    // 请求超时时间（10秒）
    TIMEOUT: 10000,
    // 客户端 API 基础路径
    CLIENT_BASE_URL: '/api/client'
}

// ==================== HTTP 状态码 ====================
export const HTTP_STATUS = {
    OK: 200,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    INTERNAL_SERVER_ERROR: 500,
    BAD_GATEWAY: 502,
    SERVICE_UNAVAILABLE: 503,
    GATEWAY_TIMEOUT: 504
}

// ==================== 业务错误码 ====================
export const BUSINESS_CODE = {
    SUCCESS: 200,
    UNAUTHORIZED: 401,
    // 用户名或密码错误
    WRONG_CREDENTIALS: 40001,
    // 账号锁定
    ACCOUNT_LOCKED: 40002,
    // 用户名已存在
    USERNAME_EXISTS: 40003,
    // 注册频繁
    REGISTER_TOO_FREQUENT: 40004
}

// ==================== 默认值 ====================
export const DEFAULTS = {
    // 空值显示
    EMPTY_TEXT: '-',
    // 默认头像
    DEFAULT_AVATAR: '/default-avatar.png',
    // 默认错误消息
    DEFAULT_ERROR_MESSAGE: '操作失败，请稍后重试'
}

// ==================== WebP 配置 ====================
export const WEBP_CONFIG = {
    // 是否启用WebP支持
    ENABLED: true,
    // 是否自动检测浏览器支持
    AUTO_DETECT: true,
    // 预估压缩率
    COMPRESSION_RATE: 0.6,
    // 支持的原始格式
    SUPPORTED_FORMATS: ['jpg', 'jpeg', 'png', 'bmp', 'gif']
}

export default {
    TOKEN_CONFIG,
    CACHE_CONFIG,
    MESSAGE_CONFIG,
    PAGINATION_CONFIG,
    VALIDATION_CONFIG,
    HTTP_CONFIG,
    HTTP_STATUS,
    BUSINESS_CODE,
    DEFAULTS,
    WEBP_CONFIG
}
