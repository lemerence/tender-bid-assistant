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

/**
 * AI 审标与编标接口。
 *
 * <p>同时提供纯 JSON 和携带附件的 multipart 两类入口，具体业务由 AI 网关服务完成。</p>
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiGatewayService aiGatewayService;

    /**
     * 创建 AI 接口控制器。
     *
     * @param aiGatewayService AI 网关业务服务
     */
    public AiController(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    /**
     * 使用文本内容执行投标文件审查。
     *
     * @param request 审标请求
     * @return 结构化审标结果
     */
    @PostMapping("/review")
    public AiReviewResponse review(@Valid @RequestBody ReviewRequest request) {
        return aiGatewayService.review(request);
    }

    /**
     * 使用文本和附件执行投标文件审查。
     *
     * @param projectId 可选的关联项目 ID
     * @param title 审查标题
     * @param tenderText 招标文件文本
     * @param bidText 投标文件文本
     * @param tenderFiles 招标文件附件
     * @param bidFiles 投标文件附件
     * @return 结构化审标结果
     */
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

    /**
     * 根据文本内容生成投标章节初稿。
     *
     * @param request 编标请求
     * @return 生成的章节内容
     */
    @PostMapping("/draft")
    public AiDraftResponse draft(@Valid @RequestBody DraftRequest request) {
        return aiGatewayService.draft(request);
    }

    /**
     * 根据文本和企业素材附件生成投标章节初稿。
     *
     * @param projectId 可选的关联项目 ID
     * @param title 标书标题
     * @param section 章节名称
     * @param tenderText 招标要求文本
     * @param knowledgeContext 企业知识库上下文
     * @param userRequirement 用户补充要求
     * @param tenderFiles 招标文件附件
     * @param materialFiles 企业素材附件
     * @return 生成的章节内容
     */
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

    /**
     * 将 multipart 表单中的可选项目 ID 转换为长整型。
     *
     * @param value 项目 ID 字符串
     * @return 有效项目 ID；未提供时返回 {@code null}
     */
    private Long parseLong(String value) {
        // 浏览器表单可能把未选择的值序列化为 undefined，需要与空值统一处理。
        if (value == null || value.isBlank() || "undefined".equals(value)) {
            return null;
        }
        return Long.parseLong(value);
    }
}
