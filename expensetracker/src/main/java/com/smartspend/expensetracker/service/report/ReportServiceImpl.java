package com.smartspend.expensetracker.service.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.smartspend.expensetracker.dto.report.ReportFilterRequest;
import com.smartspend.expensetracker.model.Transaction;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.repository.specification.TransactionSpecification;
import com.smartspend.expensetracker.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    private List<Transaction> getFilteredTransactions(ReportFilterRequest filter) {

        User user = userService.getCurrentUser();

        return transactionRepository.findAll(
                TransactionSpecification.filterBy(user.getId(),
                        new com.smartspend.expensetracker.dto.transaction.TransactionFilterRequest(
                                null,
                                filter.type(),
                                filter.categoryId(),
                                filter.startDate(),
                                filter.endDate(),
                                null,
                                null,
                                "transactionDate",
                                "desc"
                        )),
                Sort.by("transactionDate").descending()
        );
    }

    @Override
    public ByteArrayInputStream exportPdf(ReportFilterRequest filter) {

        try {

            List<Transaction> transactions = getFilteredTransactions(filter);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Smart Spend Transaction Report"));

            Table table = new Table(5);

            table.addHeaderCell("Date");
            table.addHeaderCell("Title");
            table.addHeaderCell("Category");
            table.addHeaderCell("Type");
            table.addHeaderCell("Amount");

            for (Transaction t : transactions) {

                table.addCell(t.getTransactionDate().toString());
                table.addCell(t.getTitle());
                table.addCell(t.getCategory().getName());
                table.addCell(t.getType().name());
                table.addCell(t.getAmount().toString());
            }

            document.add(table);
            document.close();

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report");
        }
    }

    @Override
    public ByteArrayInputStream exportExcel(ReportFilterRequest filter) {

        try {

            List<Transaction> transactions = getFilteredTransactions(filter);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Transactions");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Type");
            header.createCell(4).setCellValue("Amount");

            int rowIdx = 1;

            for (Transaction t : transactions) {

                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(t.getTransactionDate().toString());
                row.createCell(1).setCellValue(t.getTitle());
                row.createCell(2).setCellValue(t.getCategory().getName());
                row.createCell(3).setCellValue(t.getType().name());
                row.createCell(4).setCellValue(t.getAmount().doubleValue());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report");
        }
    }
}
