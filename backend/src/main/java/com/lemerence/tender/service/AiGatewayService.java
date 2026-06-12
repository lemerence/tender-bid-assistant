package com.lemerence.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemerence.tender.config.AppProperties;
import com.lemerence.tender.dto.AiDraftResponse;
import com.lemerence.tender.dto.AiReviewResponse;
import com.lemerence.tender.dto.DraftRequest;
import com.lemerence.tender.dto.ReviewRequest;
import com.lemerence.tender.model.DraftDocument;
import com.lemerence.tender.model.ReviewReport;
import com.lemerence.tender.repository.BidProjectRepository;
import com.lemerence.tender.repository.DraftDocumentRepository;
import com.lemerence.tender.repository.ReviewReportRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiGatewayService {
    private final HttpClient httpClient;
    private final String aiServiceUrl;
    private final ObjectMapper objectMapper;
    private final BidProjectRepository projectRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final DraftDocumentRepository draftDocumentRepository;

    public AiGatewayService(
            AppProperties properties,
            ObjectMapper objectMapper,
            BidProjectRepository projectRepository,
            ReviewReportRepository reviewReportRepository,
            DraftDocumentRepository draftDocumentRepository
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.aiServiceUrl = properties.aiServiceUrl().replaceAll("/+$", "");
        this.objectMapper = objectMapper;
        this.projectRepository = projectRepository;
        this.reviewReportRepository = reviewReportRepository;
        this.draftDocumentRepository = draftDocumentRepository;
    }

    @Transactional
    public AiReviewResponse review(ReviewRequest request) {
        AiReviewResponse response;
        try {
            response = postJson("/review", request, AiReviewResponse.class);
        } catch (IllegalStateException e) {
            response = fallbackReview(request, e);
        }

        if (response == null) {
            throw new IllegalStateException("AI review service returned an empty response");
        }

        ReviewReport report = new ReviewReport();
        report.setTitle(request.title());
        report.setRiskLevel(response.riskLevel());
        report.setTenderText(request.tenderText());
        report.setBidText(request.bidText());
        report.setReportJson(toJson(response));
        if (request.projectId() != null) {
            projectRepository.findById(request.projectId()).ifPresent(report::setProject);
        }
        reviewReportRepository.save(report);
        return response;
    }

    @Transactional
    public AiDraftResponse draft(DraftRequest request) {
        AiDraftResponse response;
        try {
            response = postJson("/draft", request, AiDraftResponse.class);
        } catch (IllegalStateException e) {
            response = fallbackDraft(request, e);
        }

        if (response == null) {
            throw new IllegalStateException("AI draft service returned an empty response");
        }

        DraftDocument document = new DraftDocument();
        document.setTitle(response.title());
        document.setSection(response.section());
        document.setPrompt(request.userRequirement() == null ? "" : request.userRequirement());
        document.setContent(response.content());
        if (request.projectId() != null) {
            projectRepository.findById(request.projectId()).ifPresent(document::setProject);
        }
        draftDocumentRepository.save(document);
        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AI response", e);
        }
    }

    private <T> T postJson(String path, Object payload, Class<T> responseType) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(aiServiceUrl + path))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("AI service returned " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to process AI service JSON", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call AI service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI service call was interrupted", e);
        }
    }

    private AiReviewResponse fallbackReview(ReviewRequest request, Exception reason) {
        List<AiReviewResponse.ReviewIssue> issues = new ArrayList<>();
        addMissingIssue(issues, request, "资质", "资格资质", "补充企业资质证书、有效期和授权证明。");
        addMissingIssue(issues, request, "报价", "报价", "核对总价、分项报价、大小写金额和最高限价响应。");
        addMissingIssue(issues, request, "盖章", "签字盖章", "检查投标函、授权书、报价表和偏离表是否完成签字盖章。");
        addMissingIssue(issues, request, "售后", "售后服务", "明确响应时间、服务周期、质保期和服务承诺。");
        addMissingIssue(issues, request, "偏离", "偏离表", "逐项填写商务和技术偏离，避免实质性负偏离。");

        String riskLevel = issues.stream().anyMatch(issue -> "高".equals(issue.severity())) ? "高" : issues.isEmpty() ? "低" : "中";
        return new AiReviewResponse(
                "已完成《" + request.title() + "》本地规则化审查，发现 " + issues.size()
                        + " 个需关注问题。AI 服务暂不可用，已使用后端兜底规则：" + reason.getClass().getSimpleName(),
                riskLevel,
                issues,
                List.of(
                        "逐项核对资格条件、评分点和废标条款",
                        "核对报价是否超过最高限价，金额大小写是否一致",
                        "检查投标函、授权书、报价表、偏离表是否签字盖章",
                        "检查技术参数和商务条款是否存在负偏离",
                        "提交前确认使用最新澄清、答疑和补遗文件"
                )
        );
    }

    private void addMissingIssue(List<AiReviewResponse.ReviewIssue> issues, ReviewRequest request, String keyword, String category, String suggestion) {
        if (request.tenderText().contains(keyword) && !request.bidText().contains(keyword)) {
            issues.add(new AiReviewResponse.ReviewIssue(
                    category,
                    List.of("资质", "报价", "盖章").contains(keyword) ? "高" : "中",
                    "招标文件出现“" + keyword + "”相关要求。",
                    "投标文件未明显检索到“" + keyword + "”相关响应。",
                    suggestion,
                    "后端本地规则检查，建议人工核对原文页码。"
            ));
        }
    }

    private AiDraftResponse fallbackDraft(DraftRequest request, Exception reason) {
        StringBuilder content = new StringBuilder();
        content.append("# ").append(request.section()).append("\n\n");
        content.append("## 一、章节目标\n");
        content.append("本章节依据招标文件要求编制，围绕“").append(request.title()).append("”项目需求进行响应，确保内容完整、表述清晰、承诺可执行。\n\n");
        content.append("## 二、响应思路\n");
        content.append("1. 充分理解招标文件中的资格、商务、技术和服务要求。\n");
        content.append("2. 结合企业既有资质、人员、业绩和服务能力进行针对性响应。\n");
        content.append("3. 对关键评分点进行重点阐述，避免遗漏实质性条款。\n\n");
        content.append("## 三、实施内容\n");
        content.append("投标人将建立专项工作机制，明确项目负责人、实施计划、质量控制、风险控制和售后服务安排，确保项目按期、按质完成。\n\n");
        if (request.userRequirement() != null && !request.userRequirement().isBlank()) {
            content.append("## 四、补充要求响应\n").append(request.userRequirement()).append("\n\n");
        }
        if (request.knowledgeContext() != null && !request.knowledgeContext().isBlank()) {
            content.append("## 五、企业能力引用\n已结合企业知识库素材进行响应，正式提交前请补充证书编号、业绩名称和证明文件页码。\n\n");
        }
        content.append("> AI 服务暂不可用，已使用后端本地模板兜底：").append(reason.getClass().getSimpleName());
        return new AiDraftResponse(request.title(), request.section(), content.toString());
    }
}
