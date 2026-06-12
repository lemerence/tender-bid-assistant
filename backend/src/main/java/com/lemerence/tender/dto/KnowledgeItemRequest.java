package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeItemRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String content,
        String tags
) {}
