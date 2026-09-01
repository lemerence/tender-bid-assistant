package com.lemerence.tender.repository;

import com.lemerence.tender.model.Attachment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 附件元数据仓储。
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    /**
     * 查询单个业务记录的全部附件。
     *
     * @param ownerType 业务归属类型
     * @param ownerId 业务记录 ID
     * @return 按创建时间倒序排列的附件列表
     */
    List<Attachment> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(String ownerType, Long ownerId);

    /**
     * 批量查询同类业务记录的附件。
     *
     * @param ownerType 业务归属类型
     * @param ownerIds 业务记录 ID 集合
     * @return 按创建时间倒序排列的附件列表
     */
    List<Attachment> findByOwnerTypeAndOwnerIdInOrderByCreatedAtDesc(String ownerType, Collection<Long> ownerIds);
}
