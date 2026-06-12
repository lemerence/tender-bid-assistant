package com.lemerence.tender.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BidProjectRequest(
        @NotBlank String projectName,
        String tenderNo,
        String tenderer,
        String agency,
        String industry,
        String region,
        BigDecimal budgetAmount,
        BigDecimal bidAmount,
        OffsetDateTime deadline,
        String status,
        String result,
        String ownerName,
        String notes
) {}
