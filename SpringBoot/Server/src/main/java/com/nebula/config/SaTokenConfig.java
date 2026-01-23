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
            /* 登录注册 */
            "/auth/login",
            "/auth/register"
    };

    /**
     * 客户端放行 URL（自动添加 /api/client 前缀）
     */
    private static final String[] CLIENT_EXCLUDE_PATHS = {
            /* 登录注册 */
            "/auth/login",
            "/auth/register",

            // 文章模块
            "/article/list",
            "/article/detail/*",
            "/article/hot",
            "/article/recommend",

            // 轮播图模块
            "/banner/list",
    };

    /**
     * 其他放行 URL
     */
    private static final String[] OTHERS_EXCLUDE_PATHS = {
            /* 自定义检测服务 */
            "/ping", // 部署测试
            "/health", // 健康检查
            "/visits", // 访问统计

            /* Spring Boot Actuator 监控端点 （按需启用）
            "/actuator/**",          // 或者更精细化控制：
            "/actuator/health",   // 健康检查（K8s/Docker常用）
            "/actuator/info",     // 应用信息
            */

            /* 调试 */
            "/test/**", // 测试接口

            /* Swagger 接口文档 */
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",

            /* 基础设施级别端点 */
            "/favicon.ico", // 浏览器获取标签页小图标
            "/error", // Spring Boot 错误错误处理端点

            /* 静态资源 */
            "/static/**",

            /* SEO 相关 */
            "/robots.txt",           // 搜索引擎爬虫规则
            "/sitemap.xml",          // 网站地图

            /* WebSocket */
            "/ws/**",                // WebSocket 端点
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