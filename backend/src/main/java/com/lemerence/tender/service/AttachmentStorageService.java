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

@Service
public class AttachmentStorageService {
    private final MinioClient minioClient;
    private final AttachmentRepository attachmentRepository;
    private final String bucket;

    public AttachmentStorageService(AppProperties properties, AttachmentRepository attachmentRepository) {
        this.minioClient = MinioClient.builder()
                .endpoint(properties.minioEndpoint())
                .credentials(properties.minioAccessKey(), properties.minioSecretKey())
                .build();
        this.attachmentRepository = attachmentRepository;
        this.bucket = properties.attachmentBucket();
    }

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

    public List<Attachment> find(String ownerType, Long ownerId) {
        return attachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ownerType, ownerId);
    }

    private Attachment store(String ownerType, Long ownerId, String usage, MultipartFile file) {
        try {
            ensureBucket();
            String originalFilename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "attachment";
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

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
