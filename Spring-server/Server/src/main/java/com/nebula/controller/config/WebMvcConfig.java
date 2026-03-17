package com.nebula.controller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 为不同模块的控制器自动添加路由前缀
 *
 * @author Nebula-Hash
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 为 @AdminController 注解的控制器添加 /admin 前缀
        configurer.addPathPrefix("/api/admin", c -> c.isAnnotationPresent(AdminController.class));
        // 为 @ClientController 注解的控制器添加 /client 前缀
        configurer.addPathPrefix("/api/client", c -> c.isAnnotationPresent(ClientController.class));
    }
}
