package com.lemerence.tender.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 企业知识库条目实体。
 *
 * <p>用于沉淀企业资质、项目业绩、人员能力和可复用投标素材。</p>
 */
@Entity
@Table(name = "knowledge_items")
public class KnowledgeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    private String tags;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    // 附件元数据由附件服务按知识条目 ID 动态查询，不直接持久化在知识表中。
    @Transient
    private List<Attachment> attachments = List.of();

    /**
     * 首次持久化前初始化创建时间和更新时间。
     */
    @PrePersist
    void onCreate() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * 实体更新前刷新最后修改时间。
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
}
