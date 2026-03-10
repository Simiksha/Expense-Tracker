package com.smartspend.expensetracker.service.report;

import java.io.ByteArrayInputStream;

import com.smartspend.expensetracker.dto.report.ReportFilterRequest;

public interface ReportService {
    ByteArrayInputStream exportPdf(ReportFilterRequest filter);

    ByteArrayInputStream exportExcel(ReportFilterRequest filter);
}
