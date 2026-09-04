package com.syfe.pfm.service;

import com.syfe.pfm.dto.MonthlyReportResponse;
import com.syfe.pfm.dto.YearlyReportResponse;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void getMonthlyReport_ShouldGroupAndAggregateCorrectly() {
        int year = 2024;
        int month = 1;

        Transaction t1 = new Transaction(userId, BigDecimal.valueOf(3000.0), LocalDate.of(2024, 1, 15), "Salary", "Salary 1", "INCOME");
        Transaction t2 = new Transaction(userId, BigDecimal.valueOf(500.0), LocalDate.of(2024, 1, 20), "Freelance", "Side gig", "INCOME");
        Transaction t3 = new Transaction(userId, BigDecimal.valueOf(400.0), LocalDate.of(2024, 1, 10), "Food", "Groceries", "EXPENSE");
        Transaction t4 = new Transaction(userId, BigDecimal.valueOf(1200.0), LocalDate.of(2024, 1, 1), "Rent", "January rent", "EXPENSE");
        Transaction t5 = new Transaction(userId, BigDecimal.valueOf(200.0), LocalDate.of(2024, 1, 25), "Transportation", "Fuel", "EXPENSE");

        when(transactionRepository.findByUserIdAndYearAndMonth(userId, year, month))
                .thenReturn(Arrays.asList(t1, t2, t3, t4, t5));

        MonthlyReportResponse report = reportService.getMonthlyReport(userId, year, month);

        assertNotNull(report);
        assertEquals(month, report.getMonth());
        assertEquals(year, report.getYear());
        
        // Income validation
        assertEquals(2, report.getTotalIncome().size());
        assertEquals(BigDecimal.valueOf(3000.0), report.getTotalIncome().get("Salary"));
        assertEquals(BigDecimal.valueOf(500.0), report.getTotalIncome().get("Freelance"));

        // Expense validation
        assertEquals(3, report.getTotalExpenses().size());
        assertEquals(BigDecimal.valueOf(400.0), report.getTotalExpenses().get("Food"));
        assertEquals(BigDecimal.valueOf(1200.0), report.getTotalExpenses().get("Rent"));
        assertEquals(BigDecimal.valueOf(200.0), report.getTotalExpenses().get("Transportation"));

        // Net savings = (3000 + 500) - (400 + 1200 + 200) = 3500 - 1800 = 1700
        assertEquals(BigDecimal.valueOf(1700.0), report.getNetSavings());
    }

    @Test
    void getYearlyReport_ShouldGroupAndAggregateCorrectly() {
        int year = 2024;

        Transaction t1 = new Transaction(userId, BigDecimal.valueOf(36000.0), LocalDate.of(2024, 6, 15), "Salary", "Yearly salary", "INCOME");
        Transaction t2 = new Transaction(userId, BigDecimal.valueOf(6000.0), LocalDate.of(2024, 8, 20), "Freelance", "Side gigs", "INCOME");
        Transaction t3 = new Transaction(userId, BigDecimal.valueOf(4800.0), LocalDate.of(2024, 3, 10), "Food", "Groceries yearly", "EXPENSE");
        Transaction t4 = new Transaction(userId, BigDecimal.valueOf(14400.0), LocalDate.of(2024, 1, 1), "Rent", "Rent yearly", "EXPENSE");
        Transaction t5 = new Transaction(userId, BigDecimal.valueOf(2400.0), LocalDate.of(2024, 10, 25), "Transportation", "Fuel yearly", "EXPENSE");

        when(transactionRepository.findByUserIdAndYear(userId, year))
                .thenReturn(Arrays.asList(t1, t2, t3, t4, t5));

        YearlyReportResponse report = reportService.getYearlyReport(userId, year);

        assertNotNull(report);
        assertEquals(year, report.getYear());
        
        // Income validation
        assertEquals(2, report.getTotalIncome().size());
        assertEquals(BigDecimal.valueOf(36000.0), report.getTotalIncome().get("Salary"));
        assertEquals(BigDecimal.valueOf(6000.0), report.getTotalIncome().get("Freelance"));

        // Expense validation
        assertEquals(3, report.getTotalExpenses().size());
        assertEquals(BigDecimal.valueOf(4800.0), report.getTotalExpenses().get("Food"));
        assertEquals(BigDecimal.valueOf(14400.0), report.getTotalExpenses().get("Rent"));
        assertEquals(BigDecimal.valueOf(2400.0), report.getTotalExpenses().get("Transportation"));

        // Net savings = (36000 + 6000) - (4800 + 14400 + 2400) = 42000 - 21600 = 20400
        assertEquals(BigDecimal.valueOf(20400.0), report.getNetSavings());
    }
}
