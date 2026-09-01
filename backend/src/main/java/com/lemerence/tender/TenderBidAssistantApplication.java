package com.lemerence.tender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 招投标助手后端应用入口。
 *
 * <p>负责启动 Spring Boot 容器并加载 Web、持久化、文件存储和 AI 网关等组件。</p>
 */
@SpringBootApplication
public class TenderBidAssistantApplication {
    /**
     * 启动后端服务。
     *
     * @param args JVM 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TenderBidAssistantApplication.class, args);
    }
}
