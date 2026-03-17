package com.nebula.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章草稿状态枚举
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Getter
@AllArgsConstructor
public enum DraftStatusEnum {

    /**
     * 已发布
     */
    PUBLISHED(0, "已发布"),

    /**
     * 草稿
     */
    DRAFT(1, "草稿");

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
    public static DraftStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DraftStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为草稿
     *
     * @param code 状态码
     * @return 是否为草稿
     */
    public static boolean isDraft(Integer code) {
        return DRAFT.getCode().equals(code);
    }

    /**
     * 判断是否已发布
     *
     * @param code 状态码
     * @return 是否已发布
     */
    public static boolean isPublished(Integer code) {
        return PUBLISHED.getCode().equals(code);
    }
}
