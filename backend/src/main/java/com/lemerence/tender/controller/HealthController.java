package com.lemerence.tender.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后端服务健康检查接口。
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    /**
     * 返回后端存活状态，供前端工作台和部署探针使用。
     *
     * @return 服务名称与健康状态
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "backend");
    }
}
