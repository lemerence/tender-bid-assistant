package com.lemerence.tender.repository;

import com.lemerence.tender.model.BidProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidProjectRepository extends JpaRepository<BidProject, Long> {
    List<BidProject> findAllByOrderByUpdatedAtDesc();
}
