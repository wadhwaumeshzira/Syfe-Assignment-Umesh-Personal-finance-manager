package com.syfe.pfm.service;

import com.syfe.pfm.dto.MonthlyReportResponse;
import com.syfe.pfm.dto.YearlyReportResponse;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndYearAndMonth(userId, year, month);

        // Group income transactions by category and sum amounts
        Map<String, BigDecimal> totalIncome = transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // Group expense transactions by category and sum amounts
        Map<String, BigDecimal> totalExpenses = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        BigDecimal sumIncome = totalIncome.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumExpense = totalExpenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = sumIncome.subtract(sumExpense);

        return new MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }

    public YearlyReportResponse getYearlyReport(Long userId, int year) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndYear(userId, year);

        // Group income transactions by category and sum amounts
        Map<String, BigDecimal> totalIncome = transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // Group expense transactions by category and sum amounts
        Map<String, BigDecimal> totalExpenses = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        BigDecimal sumIncome = totalIncome.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumExpense = totalExpenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = sumIncome.subtract(sumExpense);

        return new YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
