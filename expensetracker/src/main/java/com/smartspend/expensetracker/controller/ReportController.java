package com.smartspend.expensetracker.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.expensetracker.dto.report.ReportFilterRequest;
import com.smartspend.expensetracker.service.report.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> exportPdf(
            ReportFilterRequest filter) {

        InputStreamResource file = new InputStreamResource(reportService.exportPdf(filter));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> exportExcel(ReportFilterRequest filter) {

        InputStreamResource file = new InputStreamResource(reportService.exportExcel(filter));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
