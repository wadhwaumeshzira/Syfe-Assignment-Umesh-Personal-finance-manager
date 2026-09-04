package com.syfe.pfm.controller;

import com.syfe.pfm.dto.MonthlyReportResponse;
import com.syfe.pfm.dto.YearlyReportResponse;
import com.syfe.pfm.security.UserPrincipal;
import com.syfe.pfm.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling reporting and financial analytics endpoints.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (month < 1 || month > 12) {
            throw new com.syfe.pfm.exception.BadRequestException("Month must be between 1 and 12");
        }
        
        MonthlyReportResponse report = reportService.getMonthlyReport(principal.getId(), year, month);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        YearlyReportResponse report = reportService.getYearlyReport(principal.getId(), year);
        return ResponseEntity.ok(report);
    }
}
