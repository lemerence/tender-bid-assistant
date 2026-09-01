package com.lemerence.tender.controller;

import com.lemerence.tender.dto.BidProjectRequest;
import com.lemerence.tender.model.BidProject;
import com.lemerence.tender.repository.BidProjectRepository;
import com.lemerence.tender.repository.DraftDocumentRepository;
import com.lemerence.tender.repository.ReviewReportRepository;
import com.lemerence.tender.service.AttachmentStorageService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 招投标项目与归档查询接口。
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final BidProjectRepository projectRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final DraftDocumentRepository draftDocumentRepository;
    private final AttachmentStorageService attachmentStorageService;

    /**
     * 创建项目控制器。
     *
     * @param projectRepository 项目仓储
     * @param reviewReportRepository 审标报告仓储
     * @param draftDocumentRepository 编标文档仓储
     * @param attachmentStorageService 附件存储服务
     */
    public ProjectController(
            BidProjectRepository projectRepository,
            ReviewReportRepository reviewReportRepository,
            DraftDocumentRepository draftDocumentRepository,
            AttachmentStorageService attachmentStorageService
    ) {
        this.projectRepository = projectRepository;
        this.reviewReportRepository = reviewReportRepository;
        this.draftDocumentRepository = draftDocumentRepository;
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * 按最近更新时间倒序返回全部项目。
     *
     * @return 项目列表
     */
    @GetMapping
    public Iterable<BidProject> list() {
        return projectRepository.findAllByOrderByUpdatedAtDesc();
    }

    /**
     * 新增招投标项目。
     *
     * @param request 项目信息
     * @return 已保存的项目
     */
    @PostMapping
    public BidProject create(@Valid @RequestBody BidProjectRequest request) {
        BidProject project = new BidProject();
        apply(project, request);
        return projectRepository.save(project);
    }

    /**
     * 更新指定项目。
     *
     * @param id 项目 ID
     * @param request 更新内容
     * @return 更新后的项目
     */
    @PutMapping("/{id}")
    public BidProject update(@PathVariable Long id, @Valid @RequestBody BidProjectRequest request) {
        BidProject project = projectRepository.findById(id).orElseThrow();
        apply(project, request);
        return projectRepository.save(project);
    }

    /**
     * 聚合项目、审标报告、编标文档和各自附件，形成完整归档视图。
     *
     * @param id 项目 ID
     * @return 项目归档数据
     */
    @GetMapping("/{id}/archive")
    public Map<String, Object> archive(@PathVariable Long id) {
        BidProject project = projectRepository.findById(id).orElseThrow();
        var reviews = reviewReportRepository.findByProjectIdOrderByCreatedAtDesc(id);
        var drafts = draftDocumentRepository.findByProjectIdOrderByCreatedAtDesc(id);
        // 附件使用独立关联表存储，返回归档数据前按业务记录 ID 动态装配。
        reviews.forEach(report -> report.setAttachments(attachmentStorageService.find("REVIEW", report.getId())));
        drafts.forEach(document -> document.setAttachments(attachmentStorageService.find("DRAFT", document.getId())));
        return Map.of(
                "project", project,
                "reviews", reviews,
                "drafts", drafts
        );
    }

    /**
     * 将项目请求字段复制到实体，并为缺省状态设置业务默认值。
     *
     * @param project 待更新项目
     * @param request 项目请求
     */
    private void apply(BidProject project, BidProjectRequest request) {
        project.setProjectName(request.projectName());
        project.setTenderNo(request.tenderNo());
        project.setTenderer(request.tenderer());
        project.setAgency(request.agency());
        project.setIndustry(request.industry());
        project.setRegion(request.region());
        project.setBudgetAmount(request.budgetAmount());
        project.setBidAmount(request.bidAmount());
        project.setDeadline(request.deadline());
        project.setStatus(request.status() == null || request.status().isBlank() ? "待评估" : request.status());
        project.setResult(request.result());
        project.setOwnerName(request.ownerName());
        project.setNotes(request.notes());
    }
}
