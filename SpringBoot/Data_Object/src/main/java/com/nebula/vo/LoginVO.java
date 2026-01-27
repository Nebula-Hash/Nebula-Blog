package com.nebula.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回结果
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * Token
     */
    private String token;

    /**
     * Token 名称
     */
    private String tokenName;

    /**
     * Token 有效期（秒）
     */
    private Long tokenTimeout;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 角色标识
     */
    private String roleKey;
}
