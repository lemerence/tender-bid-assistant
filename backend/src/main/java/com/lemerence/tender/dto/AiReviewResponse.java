package com.lemerence.tender.dto;

import java.util.List;

/**
 * AI 审标服务返回的结构化审查结果。
 *
 * @param summary 审查摘要
 * @param riskLevel 总体风险等级
 * @param issues 问题明细
 * @param checklist 提交前检查项
 */
public record AiReviewResponse(
        String summary,
        String riskLevel,
        List<ReviewIssue> issues,
        List<String> checklist
) {
    /**
     * 单个审标问题及修改建议。
     *
     * @param category 问题分类
     * @param severity 严重程度
     * @param requirement 招标要求依据
     * @param finding 投标文件发现
     * @param suggestion 修改建议
     * @param source 规则或原文来源说明
     */
    public record ReviewIssue(
            String category,
            String severity,
            String requirement,
            String finding,
            String suggestion,
            String source
    ) {}
}
