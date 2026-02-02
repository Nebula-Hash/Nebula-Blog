package com.nebula.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户管理视图对象（列表/详情）
 *
 * @author Nebula-Hash
 * @date 2026/1/29
 */
@Data
public class UserAdminVO {

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
     * 头像
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String intro;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
