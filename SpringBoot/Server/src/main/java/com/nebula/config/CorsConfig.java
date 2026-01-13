package com.nebula.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Configuration  // 标记为Spring配置类，启动时自动加载
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean  // 注册为Spring Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        allowedOrigins.forEach(config::addAllowedOriginPattern);  // 允许的请求来源域名
        config.addAllowedHeader("*");         // 允许所有请求头
        config.addAllowedMethod("*");         // 允许所有HTTP方法(GET/POST/PUT/DELETE等)
        config.setAllowCredentials(true);     // 允许携带Cookie/认证信息
        config.setMaxAge(3600L);              // 预检请求缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // 对所有路径生效
        return new CorsFilter(source);
    }
}
