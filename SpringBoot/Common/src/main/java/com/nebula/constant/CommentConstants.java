package com.nebula.constant;

/**
 * 评论模块常量
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
public final class CommentConstants {

    private CommentConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 操作消息 ====================

    /**
     * 审核成功
     */
    public static final String MSG_AUDIT_SUCCESS = "审核成功";

    /**
     * 删除成功
     */
    public static final String MSG_DELETE_SUCCESS = "删除成功";

    /**
     * 批量审核成功
     */
    public static final String MSG_BATCH_AUDIT_SUCCESS = "批量审核成功";

    /**
     * 批量删除成功
     */
    public static final String MSG_BATCH_DELETE_SUCCESS = "批量删除成功";

    // ==================== 错误消息 ====================

    /**
     * 评论不存在
     */
    public static final String ERR_COMMENT_NOT_FOUND = "评论不存在";

    /**
     * 评论ID列表不能为空
     */
    public static final String ERR_COMMENT_IDS_EMPTY = "评论ID列表不能为空";

    /**
     * 无效的审核状态
     */
    public static final String ERR_INVALID_AUDIT_STATUS = "无效的审核状态";
}
