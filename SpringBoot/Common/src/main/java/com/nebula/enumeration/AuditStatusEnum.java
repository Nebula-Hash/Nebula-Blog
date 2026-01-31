package com.nebula.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论审核状态枚举
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 审核通过
     */
    APPROVED(1, "审核通过"),

    /**
     * 审核拒绝
     */
    REJECTED(2, "审核拒绝");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举实例
     */
    public static AuditStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否待审核
     *
     * @param code 状态码
     * @return 是否待审核
     */
    public static boolean isPending(Integer code) {
        return PENDING.getCode().equals(code);
    }

    /**
     * 判断是否审核通过
     *
     * @param code 状态码
     * @return 是否审核通过
     */
    public static boolean isApproved(Integer code) {
        return APPROVED.getCode().equals(code);
    }

    /**
     * 判断是否审核拒绝
     *
     * @param code 状态码
     * @return 是否审核拒绝
     */
    public static boolean isRejected(Integer code) {
        return REJECTED.getCode().equals(code);
    }
}
