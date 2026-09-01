package com.lemerence.tender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用自定义配置项。
 *
 * @param aiServiceUrl AI 服务基础地址
 * @param corsAllowedOrigins 允许跨域访问的前端来源，多个来源以逗号分隔
 * @param minioEndpoint MinIO 服务地址
 * @param minioAccessKey MinIO 访问标识
 * @param minioSecretKey MinIO 访问密钥
 * @param attachmentBucket 附件对象存储桶名称
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String aiServiceUrl,
        String corsAllowedOrigins,
        String minioEndpoint,
        String minioAccessKey,
        String minioSecretKey,
        String attachmentBucket
) {}
