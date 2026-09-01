package com.lemerence.tender.dto;

/**
 * AI 编标服务返回的章节初稿。
 *
 * @param title 文档标题
 * @param section 章节名称
 * @param content 生成的正文内容
 */
public record AiDraftResponse(
        String title,
        String section,
        String content
) {}
