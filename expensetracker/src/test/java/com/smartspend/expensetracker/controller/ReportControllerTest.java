package com.smartspend.expensetracker.controller;

import com.smartspend.expensetracker.dto.report.ReportFilterRequest;
import com.smartspend.expensetracker.enums.TransactionType;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtService;
import com.smartspend.expensetracker.service.report.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ReportService reportService;

    @Test
    void exportPdf_shouldReturnPdfFile() throws Exception {
        byte[] pdfBytes = "dummy pdf content".getBytes();

        given(reportService.exportPdf(any(ReportFilterRequest.class)))
                .willReturn(new ByteArrayInputStream(pdfBytes));

        mockMvc.perform(get("/api/reports/pdf")
                        .param("type", TransactionType.EXPENSE.name())
                        .param("categoryId", "1")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.pdf"))
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void exportExcel_shouldReturnExcelFile() throws Exception {
        byte[] excelBytes = "dummy excel content".getBytes();

        given(reportService.exportExcel(any(ReportFilterRequest.class)))
                .willReturn(new ByteArrayInputStream(excelBytes));

        mockMvc.perform(get("/api/reports/excel")
                        .param("type", TransactionType.INCOME.name())
                        .param("categoryId", "2")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.xlsx"))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
    }

    @Test
    void exportPdf_shouldWorkWithoutFilters() throws Exception {
        byte[] pdfBytes = "dummy pdf content".getBytes();

        given(reportService.exportPdf(any(ReportFilterRequest.class)))
                .willReturn(new ByteArrayInputStream(pdfBytes));

        mockMvc.perform(get("/api/reports/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.pdf"));
    }

    @Test
    void exportExcel_shouldWorkWithoutFilters() throws Exception {
        byte[] excelBytes = "dummy excel content".getBytes();

        given(reportService.exportExcel(any(ReportFilterRequest.class)))
                .willReturn(new ByteArrayInputStream(excelBytes));

        mockMvc.perform(get("/api/reports/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.xlsx"));
    }
}
