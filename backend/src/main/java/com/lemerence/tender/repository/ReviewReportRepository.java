package com.lemerence.tender.repository;

import com.lemerence.tender.model.ReviewReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    List<ReviewReport> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
