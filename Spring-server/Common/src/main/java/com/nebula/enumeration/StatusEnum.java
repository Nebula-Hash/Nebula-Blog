package com.nebula.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author Nebula-Hash
 * @date 2026/1/29
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

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
    public static StatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否启用
     *
     * @param code 状态码
     * @return 是否启用
     */
    public static boolean isEnabled(Integer code) {
        return ENABLED.getCode().equals(code);
    }

    /**
     * 判断是否禁用
     *
     * @param code 状态码
     * @return 是否禁用
     */
    public static boolean isDisabled(Integer code) {
        return DISABLED.getCode().equals(code);
    }
}
