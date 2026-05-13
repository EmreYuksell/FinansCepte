package com.finanscepte.report.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.report.model.Report;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends GenericRepository<Report, String> {

    List<Report> findByUserId(String userId);

    List<Report> findByUserIdAndType(String userId, String type);
}
