package com.lemerence.tender.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 附件元数据实体。
 *
 * <p>实际文件保存在对象存储中，本实体记录文件的业务归属、对象键和展示信息。</p>
 */
@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerType;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String usage;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String objectKey;

    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    /**
     * 首次持久化前记录附件创建时间。
     */
    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getUsage() { return usage; }
    public void setUsage(String usage) { this.usage = usage; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
