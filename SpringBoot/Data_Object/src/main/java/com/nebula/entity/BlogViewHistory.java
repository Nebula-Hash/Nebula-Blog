package com.nebula.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 浏览记录实体类
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Data
@TableName("blog_view_history")
public class BlogViewHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID(未登录则为NULL)
     */
    private Long userId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 浏览时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
