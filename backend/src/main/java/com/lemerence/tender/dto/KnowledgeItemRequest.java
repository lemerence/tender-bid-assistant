package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增或更新知识条目的请求数据。
 *
 * @param title 标题
 * @param category 分类
 * @param content 正文
 * @param tags 标签文本
 */
public record KnowledgeItemRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String content,
        String tags
) {}
