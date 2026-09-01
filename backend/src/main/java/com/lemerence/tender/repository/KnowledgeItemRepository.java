package com.lemerence.tender.repository;

import com.lemerence.tender.model.KnowledgeItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 企业知识条目仓储。
 */
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long> {
    /**
     * 按标题或正文关键词模糊检索最近更新的知识条目。
     *
     * @param titleKeyword 标题关键词
     * @param contentKeyword 正文关键词
     * @return 最多二十条知识记录
     */
    List<KnowledgeItem> findTop20ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(
            String titleKeyword,
            String contentKeyword
    );
}
