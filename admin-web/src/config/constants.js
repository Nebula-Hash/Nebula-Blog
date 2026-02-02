/**
 * 全局常量配置
 * 统一管理魔法数字和硬编码值
 */

// ==================== Token 配置 ====================
export const TOKEN_CONFIG = {
    // Token 刷新阈值（5分钟）
    REFRESH_THRESHOLD: 5 * 60 * 1000,
    // Token 刷新超时时间（10秒）
    REFRESH_TIMEOUT: 10000,
    // 请求队列最大长度
    MAX_QUEUE_SIZE: 50,
    // Token 存储键前缀
    KEY_PREFIX: 'admin_'
}

// ==================== 缓存配置 ====================
export const CACHE_CONFIG = {
    // 用户信息缓存时长（5分钟）
    USER_INFO_TTL: 5 * 60 * 1000,
    // 错误消息缓存时长（1秒）
    ERROR_MESSAGE_DURATION: 1000
}

// ==================== 消息提示配置 ====================
export const MESSAGE_CONFIG = {
    // 成功消息显示时长（2秒）
    SUCCESS_DURATION: 2000,
    // 错误消息显示时长（3秒）
    ERROR_DURATION: 3000,
    // 信息消息显示时长（2秒）
    INFO_DURATION: 2000,
    // 警告消息显示时长（2.5秒）
    WARNING_DURATION: 2500
}

// ==================== 分页配置 ====================
export const PAGINATION_CONFIG = {
    // 默认每页大小
    DEFAULT_PAGE_SIZE: 10,
    // 每页大小选项
    PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
    // 是否显示每页大小选择器
    SHOW_SIZE_PICKER: true
}

// ==================== 表单验证配置 ====================
export const VALIDATION_CONFIG = {
    // 用户名长度限制
    USERNAME_MIN_LENGTH: 2,
    USERNAME_MAX_LENGTH: 20,
    // 密码长度限制
    PASSWORD_MIN_LENGTH: 6,
    PASSWORD_MAX_LENGTH: 20,
    // 昵称长度限制
    NICKNAME_MAX_LENGTH: 30,
    // 个人简介长度限制
    INTRO_MAX_LENGTH: 200,
    // 搜索关键词最小长度
    SEARCH_KEYWORD_MIN_LENGTH: 2
}

// ==================== 文件上传配置 ====================
export const UPLOAD_CONFIG = {
    // 最大文件数量
    MAX_FILE_COUNT: 1,
    // 图片最大大小（5MB）
    MAX_IMAGE_SIZE: 5 * 1024 * 1024,
    // 允许的图片格式
    ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
    // 允许的图片扩展名
    ALLOWED_IMAGE_EXTENSIONS: ['.jpg', '.jpeg', '.png', '.gif', '.webp']
}

// ==================== 用户状态 ====================
export const USER_STATUS = {
    ENABLED: 1,
    DISABLED: 0
}

// ==================== 文章状态 ====================
export const ARTICLE_STATUS = {
    PUBLISHED: 0,
    DRAFT: 1
}

// ==================== 置顶状态 ====================
export const TOP_STATUS = {
    YES: 1,
    NO: 0
}

// ==================== 评论审核状态 ====================
export const COMMENT_AUDIT_STATUS = {
    PENDING: 0,      // 待审核
    APPROVED: 1,     // 审核通过
    REJECTED: 2      // 审核拒绝
}

// ==================== 评论审核状态映射 ====================
export const COMMENT_AUDIT_STATUS_MAP = {
    [COMMENT_AUDIT_STATUS.PENDING]: { label: '待审核', type: 'warning' },
    [COMMENT_AUDIT_STATUS.APPROVED]: { label: '审核通过', type: 'success' },
    [COMMENT_AUDIT_STATUS.REJECTED]: { label: '审核拒绝', type: 'error' }
}

// ==================== HTTP 状态码 ====================
export const HTTP_STATUS = {
    SUCCESS: 200,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    SERVER_ERROR: 500
}

// ==================== 业务错误码 ====================
export const BUSINESS_CODE = {
    SUCCESS: 200,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    // 用户名或密码错误
    INVALID_CREDENTIALS: 40001,
    // 账号锁定
    ACCOUNT_LOCKED: 40002,
    // 用户名已存在
    USERNAME_EXISTS: 40003,
    // 注册频繁
    REGISTER_FREQUENT: 40004
}

// ==================== 路由路径 ====================
export const ROUTES = {
    LOGIN: '/login',
    HOME: '/',
    DATA_PANEL: '/datapanel',
    ARTICLES: '/articles',
    CATEGORIES: '/categories',
    TAGS: '/tags',
    USERS: '/users',
    BANNERS: '/banners',
    COMMENTS: '/comments'
}

// ==================== API 基础路径 ====================
export const API_CONFIG = {
    BASE_URL: '/api/admin',
    TIMEOUT: 10000
}

// ==================== 默认值 ====================
export const DEFAULTS = {
    // 默认头像占位符
    AVATAR_PLACEHOLDER: 'U',
    // 默认空值显示
    EMPTY_TEXT: '-',
    // 默认错误消息
    DEFAULT_ERROR_MESSAGE: '操作失败，请稍后重试'
}

export default {
    TOKEN_CONFIG,
    CACHE_CONFIG,
    MESSAGE_CONFIG,
    PAGINATION_CONFIG,
    VALIDATION_CONFIG,
    UPLOAD_CONFIG,
    USER_STATUS,
    ARTICLE_STATUS,
    TOP_STATUS,
    COMMENT_AUDIT_STATUS,
    COMMENT_AUDIT_STATUS_MAP,
    HTTP_STATUS,
    BUSINESS_CODE,
    ROUTES,
    API_CONFIG,
    DEFAULTS
}
