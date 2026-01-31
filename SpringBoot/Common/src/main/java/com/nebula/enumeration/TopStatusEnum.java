package com.nebula.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章置顶状态枚举
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Getter
@AllArgsConstructor
public enum TopStatusEnum {

    /**
     * 普通
     */
    NORMAL(0, "普通"),

    /**
     * 置顶
     */
    TOP(1, "置顶");

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
    public static TopStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TopStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否置顶
     *
     * @param code 状态码
     * @return 是否置顶
     */
    public static boolean isTop(Integer code) {
        return TOP.getCode().equals(code);
    }

    /**
     * 判断是否普通
     *
     * @param code 状态码
     * @return 是否普通
     */
    public static boolean isNormal(Integer code) {
        return NORMAL.getCode().equals(code);
    }
}
