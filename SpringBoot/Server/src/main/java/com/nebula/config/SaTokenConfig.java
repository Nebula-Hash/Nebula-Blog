package com.nebula.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.nebula.service.authority.helper.AuthHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
            /* 登录 */
            "/auth/login"
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

            // 分类模块
            "/category/list",
            "/category/detail/*",

            // 标签模块
            "/tag/list",
            "/tag/detail/*",
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
        // 构建管理端和客户端的放行路径
        List<String> adminExcludePaths = buildAdminExcludePaths();
        List<String> clientExcludePaths = buildClientExcludePaths();
        List<String> otherExcludePaths = Arrays.asList(OTHERS_EXCLUDE_PATHS);

        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 管理端接口：需要登录 + admin 角色
                    SaRouter.match(ADMIN_PREFIX + "/**")
                            .notMatch(adminExcludePaths)
                            .check(r -> {
                                StpUtil.checkLogin();
                                StpUtil.checkRole(AuthHelper.ADMIN_ROLE_KEY);
                            });

                    // 客户端接口：只需要登录
                    SaRouter.match(CLIENT_PREFIX + "/**")
                            .notMatch(clientExcludePaths)
                            .check(r -> StpUtil.checkLogin());

                    // 其他接口：放行或登录校验
                    SaRouter.match("/**")
                            .notMatch(ADMIN_PREFIX + "/**")
                            .notMatch(CLIENT_PREFIX + "/**")
                            .notMatch(otherExcludePaths)
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }

    /**
     * 构建管理端放行路径列表
     */
    private List<String> buildAdminExcludePaths() {
        return Arrays.stream(ADMIN_EXCLUDE_PATHS)
                .map(path -> ADMIN_PREFIX + path)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 构建客户端放行路径列表
     */
    private List<String> buildClientExcludePaths() {
        return Arrays.stream(CLIENT_EXCLUDE_PATHS)
                .map(path -> CLIENT_PREFIX + path)
                .collect(java.util.stream.Collectors.toList());
    }
}