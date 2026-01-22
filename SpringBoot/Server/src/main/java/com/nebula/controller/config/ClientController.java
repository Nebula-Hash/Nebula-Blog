package com.nebula.controller.config;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * Client 模块控制器注解
 * 标记为 Client 模块的 REST 控制器
 * 通过 WebMvcConfig 自动添加 /client 路由前缀
 *
 * @author Nebula-Hash
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface ClientController {
}
