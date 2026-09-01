package com.lemerence.tender.repository;

import com.lemerence.tender.model.BidProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 招投标项目仓储。
 */
public interface BidProjectRepository extends JpaRepository<BidProject, Long> {
    /**
     * 查询全部项目并按最近更新时间倒序排列。
     *
     * @return 项目列表
     */
    List<BidProject> findAllByOrderByUpdatedAtDesc();
}
