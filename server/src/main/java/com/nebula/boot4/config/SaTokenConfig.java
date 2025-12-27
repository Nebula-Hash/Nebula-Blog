package com.nebula.boot4.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 指定路由进行登录校验
                    SaRouter.match("/**")
                            // 排除不需要登录的路径
                            .notMatch("/auth/login")
                            .notMatch("/auth/register")
                            .notMatch("/")
                            .notMatch("/favicon.ico")
                            .notMatch("/error")
                            .notMatch("/article/list")
                            .notMatch("/article/{id}")
                            .notMatch("/article/hot")
                            .notMatch("/article/recommend")
                            .notMatch("/category/list")
                            .notMatch("/tag/list")
                            .notMatch("/comment/list/**")
                            .notMatch("/banner/list")
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }
}
