package com.lemerence.tender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String aiServiceUrl,
        String corsAllowedOrigins,
        String minioEndpoint,
        String minioAccessKey,
        String minioSecretKey,
        String attachmentBucket
) {}
