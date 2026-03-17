package com.nebula.controller.config;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * Admin 模块控制器注解
 * 标记为 Admin 模块的 REST 控制器
 * 通过 WebMvcConfig 自动添加 /admin 路由前缀
 *
 * @author Nebula-Hash
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface AdminController {
}
