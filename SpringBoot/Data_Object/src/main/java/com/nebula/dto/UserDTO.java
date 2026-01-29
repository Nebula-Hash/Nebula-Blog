package com.nebula.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户数据传输对象（新增/编辑）
 *
 * @author Nebula-Hash
 * @date 2026/1/29
 */
@Data
public class UserDTO {

    /**
     * 用户ID（编辑时必填）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度需在2-20个字符之间")
    private String username;

    /**
     * 密码（新增时必填）
     */
    @Size(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
    private String password;

    /**
     * 昵称
     */
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人简介
     */
    @Size(max = 200, message = "个人简介不能超过200个字符")
    private String intro;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}
