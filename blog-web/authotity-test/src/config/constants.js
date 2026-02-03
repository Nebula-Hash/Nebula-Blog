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

export default {
    TOKEN_CONFIG,
    CACHE_CONFIG,
    VALIDATION_CONFIG,
    BUSINESS_CODE,
}