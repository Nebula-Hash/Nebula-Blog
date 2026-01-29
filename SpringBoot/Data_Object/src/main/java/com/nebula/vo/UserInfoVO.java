package com.nebula.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回对象
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String intro;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
