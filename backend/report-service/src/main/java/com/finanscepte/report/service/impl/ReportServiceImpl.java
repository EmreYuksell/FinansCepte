package com.finanscepte.report.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.dto.ReportResponse;
import com.finanscepte.report.model.Report;
import com.finanscepte.report.model.ReportType;
import com.finanscepte.report.repository.ReportRepository;
import com.finanscepte.report.service.ReportService;
import com.finanscepte.report.util.ReportMapper;
import com.finanscepte.report.strategy.ReportGenerationStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final Map<String, ReportGenerationStrategy> strategies;

    public ReportServiceImpl(ReportRepository reportRepository, ReportMapper reportMapper,
                             List<ReportGenerationStrategy> strategyList) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.strategies = strategyList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.getClass().getSimpleName().replace("Strategy", ""),
                        s -> s
                ));
    }

    @Override
    public ReportResponse generate(ReportRequest request) {
        ReportType type = ReportType.valueOf(request.type());
        ReportGenerationStrategy strategy = strategies.get(type.name().replace("_", ""));
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for report type: " + request.type());
        }
        Report report = reportMapper.toEntity(request);
        report.setData(strategy.generate(request));
        Report saved = reportRepository.save(report);
        return reportMapper.toResponse(saved);
    }

    @Override
    public ReportResponse findById(String id) {
        return reportRepository.findById(id)
                .map(reportMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }

    @Override
    public List<ReportResponse> findAll() {
        return reportRepository.findAll().stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> findByUserId(String userId) {
        return reportRepository.findByUserId(userId).stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report", "id", id);
        }
        reportRepository.deleteById(id);
    }
}
