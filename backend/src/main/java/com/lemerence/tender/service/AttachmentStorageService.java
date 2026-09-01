package com.lemerence.tender.service;

import com.lemerence.tender.config.AppProperties;
import com.lemerence.tender.model.Attachment;
import com.lemerence.tender.repository.AttachmentRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件对象存储与元数据管理服务。
 *
 * <p>文件内容写入 MinIO，文件归属、原始名称和大小等信息写入关系数据库。</p>
 */
@Service
public class AttachmentStorageService {
    private final MinioClient minioClient;
    private final AttachmentRepository attachmentRepository;
    private final String bucket;

    /**
     * 创建附件存储服务和 MinIO 客户端。
     *
     * @param properties 应用配置
     * @param attachmentRepository 附件元数据仓储
     */
    public AttachmentStorageService(AppProperties properties, AttachmentRepository attachmentRepository) {
        this.minioClient = MinioClient.builder()
                .endpoint(properties.minioEndpoint())
                .credentials(properties.minioAccessKey(), properties.minioSecretKey())
                .build();
        this.attachmentRepository = attachmentRepository;
        this.bucket = properties.attachmentBucket();
    }

    /**
     * 批量保存非空附件。
     *
     * @param ownerType 业务归属类型，例如 REVIEW、DRAFT 或 KNOWLEDGE
     * @param ownerId 业务记录 ID
     * @param usage 附件用途说明
     * @param files 待上传文件
     * @return 已保存的附件元数据列表
     */
    public List<Attachment> storeAll(String ownerType, Long ownerId, String usage, MultipartFile[] files) {
        List<Attachment> saved = new ArrayList<>();
        if (files == null) {
            return saved;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saved.add(store(ownerType, ownerId, usage, file));
            }
        }
        return saved;
    }

    /**
     * 查询指定业务记录的附件元数据。
     *
     * @param ownerType 业务归属类型
     * @param ownerId 业务记录 ID
     * @return 按创建时间倒序排列的附件列表
     */
    public List<Attachment> find(String ownerType, Long ownerId) {
        return attachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ownerType, ownerId);
    }

    /**
     * 将单个文件写入 MinIO，并持久化其关系型元数据。
     *
     * @param ownerType 业务归属类型
     * @param ownerId 业务记录 ID
     * @param usage 附件用途
     * @param file 待上传文件
     * @return 已保存的附件元数据
     */
    private Attachment store(String ownerType, Long ownerId, String usage, MultipartFile file) {
        try {
            ensureBucket();
            String originalFilename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "attachment";
            // UUID 防止同名文件覆盖，业务类型和记录 ID 前缀便于按归属定位对象。
            String objectKey = ownerType.toLowerCase() + "/" + ownerId + "/" + UUID.randomUUID() + "-" + originalFilename;
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            Attachment attachment = new Attachment();
            attachment.setOwnerType(ownerType);
            attachment.setOwnerId(ownerId);
            attachment.setUsage(usage);
            attachment.setOriginalFilename(originalFilename);
            attachment.setObjectKey(objectKey);
            attachment.setContentType(file.getContentType());
            attachment.setSizeBytes(file.getSize());
            return attachmentRepository.save(attachment);
        } catch (Exception e) {
            throw new IllegalStateException("附件上传失败：" + file.getOriginalFilename(), e);
        }
    }

    /**
     * 确保配置的附件存储桶存在。
     *
     * @throws Exception 当 MinIO 查询或创建存储桶失败时抛出
     */
    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
