package com.lemerence.tender.repository;

import com.lemerence.tender.model.ReviewReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AI 审标报告仓储。
 */
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    /**
     * 查询指定项目的审标报告。
     *
     * @param projectId 项目 ID
     * @return 按创建时间倒序排列的审标报告
     */
    List<ReviewReport> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
