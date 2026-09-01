package com.lemerence.tender.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * AI 生成的投标章节文档实体。
 *
 * <p>正文和生成条件持久化到数据库，附件列表在查询归档时动态装配。</p>
 */
@Entity
@Table(name = "draft_documents")
public class DraftDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private BidProject project;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    // 附件元数据位于独立表中，禁止 JPA 将聚合结果映射为当前表字段。
    @Transient
    private List<Attachment> attachments = List.of();

    /**
     * 首次持久化前记录文档生成时间。
     */
    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BidProject getProject() { return project; }
    public void setProject(BidProject project) { this.project = project; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
}
