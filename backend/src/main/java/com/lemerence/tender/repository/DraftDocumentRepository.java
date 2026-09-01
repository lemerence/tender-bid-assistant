package com.lemerence.tender.repository;

import com.lemerence.tender.model.DraftDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AI 编标文档仓储。
 */
public interface DraftDocumentRepository extends JpaRepository<DraftDocument, Long> {
    /**
     * 查询指定项目的编标记录。
     *
     * @param projectId 项目 ID
     * @return 按创建时间倒序排列的编标文档
     */
    List<DraftDocument> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
