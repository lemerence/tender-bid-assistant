package com.lemerence.tender.repository;

import com.lemerence.tender.model.Attachment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(String ownerType, Long ownerId);

    List<Attachment> findByOwnerTypeAndOwnerIdInOrderByCreatedAtDesc(String ownerType, Collection<Long> ownerIds);
}
