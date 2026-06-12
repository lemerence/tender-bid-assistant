package com.lemerence.tender.controller;

import com.lemerence.tender.dto.BidProjectRequest;
import com.lemerence.tender.model.BidProject;
import com.lemerence.tender.repository.BidProjectRepository;
import com.lemerence.tender.repository.DraftDocumentRepository;
import com.lemerence.tender.repository.ReviewReportRepository;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final BidProjectRepository projectRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final DraftDocumentRepository draftDocumentRepository;

    public ProjectController(
            BidProjectRepository projectRepository,
            ReviewReportRepository reviewReportRepository,
            DraftDocumentRepository draftDocumentRepository
    ) {
        this.projectRepository = projectRepository;
        this.reviewReportRepository = reviewReportRepository;
        this.draftDocumentRepository = draftDocumentRepository;
    }

    @GetMapping
    public Iterable<BidProject> list() {
        return projectRepository.findAllByOrderByUpdatedAtDesc();
    }

    @PostMapping
    public BidProject create(@Valid @RequestBody BidProjectRequest request) {
        BidProject project = new BidProject();
        apply(project, request);
        return projectRepository.save(project);
    }

    @PutMapping("/{id}")
    public BidProject update(@PathVariable Long id, @Valid @RequestBody BidProjectRequest request) {
        BidProject project = projectRepository.findById(id).orElseThrow();
        apply(project, request);
        return projectRepository.save(project);
    }

    @GetMapping("/{id}/archive")
    public Map<String, Object> archive(@PathVariable Long id) {
        BidProject project = projectRepository.findById(id).orElseThrow();
        return Map.of(
                "project", project,
                "reviews", reviewReportRepository.findByProjectIdOrderByCreatedAtDesc(id),
                "drafts", draftDocumentRepository.findByProjectIdOrderByCreatedAtDesc(id)
        );
    }

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
