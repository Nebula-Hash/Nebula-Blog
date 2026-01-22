package com.nebula.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 配置类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final String API_PREFIX = "/api";
    private static final String ADMIN_PREFIX = API_PREFIX + "/admin";
    private static final String CLIENT_PREFIX = API_PREFIX + "/client";

    /**
     * 管理端放行 URL（自动添加 /api/admin 前缀）
     */
    private static final String[] ADMIN_EXCLUDE_PATHS = {
            "/auth/login",
            "/auth/register"
    };

    /**
     * 客户端放行 URL（自动添加 /api/client 前缀）
     */
    private static final String[] CLIENT_EXCLUDE_PATHS = {
            "/auth/login",
            "/auth/register",

            // 文章模块（公开接口）
            "/article/list",
            "/article/detail/*",
            "/article/hot",
            "/article/recommend",
    };

    /**
     * 其他放行 URL
     */
    private static final String[] OTHERS_EXCLUDE_PATHS = {
            "/",
            "/favicon.ico",
            "/error",

            "/test/**", // 测试接口

            /* Swagger 接口文档相关 */
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePaths = buildExcludePaths();

        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/**")
                            .notMatch(excludePaths) // 放行指定路径
                            .check(r -> StpUtil.checkLogin()); // 校验规则为 StpUtil.checkLogin() 登录校验
                }))
                .addPathPatterns("/**");
    }

    /**
     * 构建所有放行路径列表
     */
    private List<String> buildExcludePaths() {
        List<String> excludePaths = new ArrayList<>();

        // 添加管理端放行路径
        Arrays.stream(ADMIN_EXCLUDE_PATHS)
                .map(path -> ADMIN_PREFIX + path)
                .forEach(excludePaths::add);

        // 添加客户端放行路径
        Arrays.stream(CLIENT_EXCLUDE_PATHS)
                .map(path -> CLIENT_PREFIX + path)
                .forEach(excludePaths::add);

        // 添加通用放行路径
        excludePaths.addAll(Arrays.asList(OTHERS_EXCLUDE_PATHS));

        return excludePaths;
    }
}