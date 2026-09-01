package com.lemerence.tender.config;

import java.util.Arrays;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层公共组件配置。
 *
 * <p>集中创建 AI 服务 HTTP 客户端，并按部署配置开放后端 API 的跨域访问。</p>
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebConfig {
    /**
     * 创建以 AI 服务地址为基础 URL 的 REST 客户端。
     *
     * @param properties 应用配置
     * @return AI 服务 REST 客户端
     */
    @Bean
    RestClient aiRestClient(AppProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.aiServiceUrl())
                .build();
    }

    /**
     * 创建 API 跨域配置器。
     *
     * @param properties 应用配置
     * @return Spring MVC 配置器
     */
    @Bean
    WebMvcConfigurer corsConfigurer(AppProperties properties) {
        return new WebMvcConfigurer() {
            /**
             * 为所有后端 API 注册允许的来源、请求方法和请求头。
             *
             * @param registry 跨域规则注册器
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 部署配置允许以逗号分隔多个前端域名；空白项必须过滤，避免生成无效规则。
                String[] origins = Arrays.stream(properties.corsAllowedOrigins().split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toArray(String[]::new);
                registry.addMapping("/api/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
