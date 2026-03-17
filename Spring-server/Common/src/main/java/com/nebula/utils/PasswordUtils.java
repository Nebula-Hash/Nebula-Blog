package com.nebula.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类（基于 BCrypt 算法）
 * <p>
 * BCrypt 特点：
 * 1. 自动加盐，相同密码每次加密结果不同
 * 2. 计算成本可调，抵抗暴力破解
 * 3. 单向不可逆，无法从哈希值还原密码
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
public class PasswordUtils {

    /**
     * BCrypt 密码编码器（线程安全，可复用）
     * strength 参数：加密强度，默认为 10，值越大越安全但越慢
     */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 私有构造函数，防止实例化
     */
    private PasswordUtils() {
    }

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码（明文）
     * @return 加密后的密码（BCrypt 哈希值）
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码是否匹配
     *
     * @param rawPassword     原始密码（用户输入的明文）
     * @param encodedPassword 加密后的密码（数据库中存储的哈希值）
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
