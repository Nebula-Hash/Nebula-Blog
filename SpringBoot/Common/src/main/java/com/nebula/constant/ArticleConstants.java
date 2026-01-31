package com.nebula.constant;

/**
 * 文章模块常量
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class ArticleConstants {

    private ArticleConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 错误消息 ====================

    /**
     * 文章不存在
     */
    public static final String ERROR_ARTICLE_NOT_FOUND = "文章不存在";

    /**
     * 标签不存在
     */
    public static final String ERROR_TAG_NOT_FOUND = "标签不存在";

    // ==================== 默认值 ====================

    /**
     * 热门文章默认数量
     */
    public static final String DEFAULT_HOT_LIMIT_STR = "5";

    /**
     * 推荐文章默认数量
     */
    public static final String DEFAULT_RECOMMEND_LIMIT_STR = "5";

    /**
     * 热门文章候选池大小（用于综合评分算法）
     */
    public static final int HOT_ARTICLE_CANDIDATE_POOL_SIZE = 100;

    // ==================== 成功消息 ====================

    /**
     * 发布成功
     */
    public static final String MSG_PUBLISH_SUCCESS = "发布成功";

    /**
     * 更新成功
     */
    public static final String MSG_UPDATE_SUCCESS = "更新成功";

    /**
     * 删除成功
     */
    public static final String MSG_DELETE_SUCCESS = "删除成功";
}
