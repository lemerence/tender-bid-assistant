package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(
        Long projectId,
        @NotBlank String title,
        @NotBlank String tenderText,
        @NotBlank String bidText
) {}
