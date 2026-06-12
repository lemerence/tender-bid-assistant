package com.lemerence.tender.controller;

import com.lemerence.tender.dto.AiDraftResponse;
import com.lemerence.tender.dto.AiReviewResponse;
import com.lemerence.tender.dto.DraftRequest;
import com.lemerence.tender.dto.ReviewRequest;
import com.lemerence.tender.service.AiGatewayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiGatewayService aiGatewayService;

    public AiController(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    @PostMapping("/review")
    public AiReviewResponse review(@Valid @RequestBody ReviewRequest request) {
        return aiGatewayService.review(request);
    }

    @PostMapping("/draft")
    public AiDraftResponse draft(@Valid @RequestBody DraftRequest request) {
        return aiGatewayService.draft(request);
    }
}
