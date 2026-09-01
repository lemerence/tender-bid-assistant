package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 编标请求。
 *
 * @param projectId 可选的关联项目 ID
 * @param title 标书标题
 * @param section 目标章节
 * @param tenderText 招标要求文本
 * @param knowledgeContext 企业知识库上下文
 * @param userRequirement 用户补充要求
 */
public record DraftRequest(
        Long projectId,
        @NotBlank String title,
        @NotBlank String section,
        @NotBlank String tenderText,
        String knowledgeContext,
        String userRequirement
) {}
