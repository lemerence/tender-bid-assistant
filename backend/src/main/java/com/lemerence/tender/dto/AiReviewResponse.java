package com.lemerence.tender.dto;

import java.util.List;

public record AiReviewResponse(
        String summary,
        String riskLevel,
        List<ReviewIssue> issues,
        List<String> checklist
) {
    public record ReviewIssue(
            String category,
            String severity,
            String requirement,
            String finding,
            String suggestion,
            String source
    ) {}
}
