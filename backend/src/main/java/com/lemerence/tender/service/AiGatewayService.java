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
import org.springframework.web.multipart.MultipartFile;

/**
 * 后端与 AI 服务之间的业务网关。
 *
 * <p>负责调用独立 AI 服务、在调用失败时执行本地兜底，并将审标或编标结果及附件归档。</p>
 */
@Service
public class AiGatewayService {
    private final HttpClient httpClient;
    private final String aiServiceUrl;
    private final ObjectMapper objectMapper;
    private final BidProjectRepository projectRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final DraftDocumentRepository draftDocumentRepository;
    private final AttachmentStorageService attachmentStorageService;

    /**
     * 创建 AI 网关服务。
     *
     * @param properties 应用配置
     * @param objectMapper JSON 序列化器
     * @param projectRepository 项目仓储
     * @param reviewReportRepository 审标报告仓储
     * @param draftDocumentRepository 编标文档仓储
     * @param attachmentStorageService 附件存储服务
     */
    public AiGatewayService(
            AppProperties properties,
            ObjectMapper objectMapper,
            BidProjectRepository projectRepository,
            ReviewReportRepository reviewReportRepository,
            DraftDocumentRepository draftDocumentRepository,
            AttachmentStorageService attachmentStorageService
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.aiServiceUrl = properties.aiServiceUrl().replaceAll("/+$", "");
        this.objectMapper = objectMapper;
        this.projectRepository = projectRepository;
        this.reviewReportRepository = reviewReportRepository;
        this.draftDocumentRepository = draftDocumentRepository;
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * 执行不带附件的审标任务。
     *
     * @param request 审标请求
     * @return 审标结果
     */
    @Transactional
    public AiReviewResponse review(ReviewRequest request) {
        return review(request, null, null);
    }

    /**
     * 执行审标任务并归档审标结果及相关附件。
     *
     * @param request 审标请求
     * @param tenderFiles 招标文件附件
     * @param bidFiles 投标文件附件
     * @return 审标结果
     */
    @Transactional
    public AiReviewResponse review(ReviewRequest request, MultipartFile[] tenderFiles, MultipartFile[] bidFiles) {
        AiReviewResponse response;
        try {
            response = postJson("/review", request, AiReviewResponse.class);
        } catch (IllegalStateException e) {
            // AI 服务不可达或返回无效数据时，继续使用本地规则生成可用结果，避免核心流程中断。
            response = fallbackReview(request, e);
        }

        if (response == null) {
            throw new IllegalStateException("AI review service returned an empty response");
        }

        // 先持久化业务结果以获取记录 ID，再以该 ID 作为附件归属键。
        ReviewReport report = new ReviewReport();
        report.setTitle(request.title());
        report.setRiskLevel(response.riskLevel());
        report.setTenderText(request.tenderText());
        report.setBidText(request.bidText());
        report.setReportJson(toJson(response));
        if (request.projectId() != null) {
            projectRepository.findById(request.projectId()).ifPresent(report::setProject);
        }
        ReviewReport saved = reviewReportRepository.save(report);
        attachmentStorageService.storeAll("REVIEW", saved.getId(), "招标文件附件", tenderFiles);
        attachmentStorageService.storeAll("REVIEW", saved.getId(), "投标文件附件", bidFiles);
        return response;
    }

    /**
     * 执行不带附件的编标任务。
     *
     * @param request 编标请求
     * @return 编标结果
     */
    @Transactional
    public AiDraftResponse draft(DraftRequest request) {
        return draft(request, null, null);
    }

    /**
     * 执行编标任务并归档生成内容及相关附件。
     *
     * @param request 编标请求
     * @param tenderFiles 招标文件附件
     * @param materialFiles 企业素材附件
     * @return 编标结果
     */
    @Transactional
    public AiDraftResponse draft(DraftRequest request, MultipartFile[] tenderFiles, MultipartFile[] materialFiles) {
        AiDraftResponse response;
        try {
            response = postJson("/draft", request, AiDraftResponse.class);
        } catch (IllegalStateException e) {
            // AI 调用失败时使用本地模板保证用户仍能获得可编辑初稿。
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
        DraftDocument saved = draftDocumentRepository.save(document);
        attachmentStorageService.storeAll("DRAFT", saved.getId(), "招标文件附件", tenderFiles);
        attachmentStorageService.storeAll("DRAFT", saved.getId(), "企业素材附件", materialFiles);
        return response;
    }

    /**
     * 将对象序列化为用于归档的 JSON 文本。
     *
     * @param value 待序列化对象
     * @return JSON 文本
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AI response", e);
        }
    }

    /**
     * 向 AI 服务发送 JSON 请求并反序列化响应。
     *
     * @param path AI 服务接口路径
     * @param payload 请求对象
     * @param responseType 响应类型
     * @param <T> 响应对象类型
     * @return AI 服务响应
     * @throws IllegalStateException 当请求、响应或 JSON 处理失败时抛出
     */
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
            // 恢复中断标记，确保上层线程池能够感知并正确处理取消信号。
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI service call was interrupted", e);
        }
    }

    /**
     * 使用关键词缺失规则生成本地审标结果。
     *
     * @param request 审标请求
     * @param reason 触发兜底的异常
     * @return 本地规则审标结果
     */
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

    /**
     * 当招标文件包含关键字而投标文件未响应时追加风险项。
     *
     * @param issues 风险项集合
     * @param request 审标请求
     * @param keyword 检查关键词
     * @param category 风险分类
     * @param suggestion 修改建议
     */
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

    /**
     * 使用本地固定结构生成可编辑的编标初稿。
     *
     * @param request 编标请求
     * @param reason 触发兜底的异常
     * @return 本地模板生成结果
     */
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
