package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 审标请求。
 *
 * @param projectId 可选的关联项目 ID
 * @param title 审查标题
 * @param tenderText 招标文件文本
 * @param bidText 投标文件文本
 */
public record ReviewRequest(
        Long projectId,
        @NotBlank String title,
        @NotBlank String tenderText,
        @NotBlank String bidText
) {}
