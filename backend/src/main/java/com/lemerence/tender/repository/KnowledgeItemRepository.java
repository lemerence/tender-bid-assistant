package com.lemerence.tender.repository;

import com.lemerence.tender.model.KnowledgeItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long> {
    List<KnowledgeItem> findTop20ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(
            String titleKeyword,
            String contentKeyword
    );
}
