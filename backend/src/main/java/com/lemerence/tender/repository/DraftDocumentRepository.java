package com.lemerence.tender.repository;

import com.lemerence.tender.model.DraftDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftDocumentRepository extends JpaRepository<DraftDocument, Long> {
    List<DraftDocument> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
