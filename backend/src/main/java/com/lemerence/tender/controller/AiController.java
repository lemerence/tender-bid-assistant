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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(path = "/review-with-files", consumes = "multipart/form-data")
    public AiReviewResponse reviewWithFiles(
            @RequestPart(value = "projectId", required = false) String projectId,
            @RequestPart("title") String title,
            @RequestPart("tenderText") String tenderText,
            @RequestPart("bidText") String bidText,
            @RequestPart(value = "tenderFiles", required = false) MultipartFile[] tenderFiles,
            @RequestPart(value = "bidFiles", required = false) MultipartFile[] bidFiles
    ) {
        return aiGatewayService.review(
                new ReviewRequest(parseLong(projectId), title, tenderText, bidText),
                tenderFiles,
                bidFiles
        );
    }

    @PostMapping("/draft")
    public AiDraftResponse draft(@Valid @RequestBody DraftRequest request) {
        return aiGatewayService.draft(request);
    }

    @PostMapping(path = "/draft-with-files", consumes = "multipart/form-data")
    public AiDraftResponse draftWithFiles(
            @RequestPart(value = "projectId", required = false) String projectId,
            @RequestPart("title") String title,
            @RequestPart("section") String section,
            @RequestPart("tenderText") String tenderText,
            @RequestPart(value = "knowledgeContext", required = false) String knowledgeContext,
            @RequestPart(value = "userRequirement", required = false) String userRequirement,
            @RequestPart(value = "tenderFiles", required = false) MultipartFile[] tenderFiles,
            @RequestPart(value = "materialFiles", required = false) MultipartFile[] materialFiles
    ) {
        return aiGatewayService.draft(
                new DraftRequest(parseLong(projectId), title, section, tenderText, knowledgeContext, userRequirement),
                tenderFiles,
                materialFiles
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank() || "undefined".equals(value)) {
            return null;
        }
        return Long.parseLong(value);
    }
}
