package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;

public record DraftRequest(
        Long projectId,
        @NotBlank String title,
        @NotBlank String section,
        @NotBlank String tenderText,
        String knowledgeContext,
        String userRequirement
) {}
