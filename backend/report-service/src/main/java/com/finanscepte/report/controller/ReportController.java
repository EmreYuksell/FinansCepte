package com.finanscepte.report.controller;

import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.dto.ReportResponse;
import com.finanscepte.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> generate(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generate(request));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> findAll() {
        return ResponseEntity.ok(reportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(reportService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportResponse>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.findByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        reportService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(@RequestParam String period) {
        return ResponseEntity.ok(reportService.getSummary(period));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> trend(@RequestParam String period) {
        return ResponseEntity.ok(reportService.getTrend(period));
    }

    @GetMapping("/category")
    public ResponseEntity<List<Map<String, Object>>> category(@RequestParam String period) {
        return ResponseEntity.ok(reportService.getCategoryBreakdown(period));
    }

    @GetMapping("/insights")
    public ResponseEntity<List<String>> insights() {
        return ResponseEntity.ok(reportService.getInsights());
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam String period, @RequestParam String userId) {
        byte[] pdf = reportService.exportPdf(period, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "ceptefinans-rapor.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
