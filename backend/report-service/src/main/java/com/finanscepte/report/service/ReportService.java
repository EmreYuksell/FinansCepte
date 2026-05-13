package com.finanscepte.report.service;

import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.dto.ReportResponse;

import java.util.List;

public interface ReportService {

    ReportResponse generate(ReportRequest request);

    ReportResponse findById(String id);

    List<ReportResponse> findAll();

    List<ReportResponse> findByUserId(String userId);

    void deleteById(String id);
}
