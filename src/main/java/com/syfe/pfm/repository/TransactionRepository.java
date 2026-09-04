package com.syfe.pfm.repository;

import com.syfe.pfm.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByUserIdAndCategoryName(Long userId, String categoryName);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:category IS NULL OR t.categoryName = :category) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findFilteredTransactions(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") String category,
            @Param("type") String type
    );

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    List<Transaction> findByUserIdAndYearAndMonth(
            @Param("userId") Long userId, 
            @Param("year") int year, 
            @Param("month") int month
    );

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND YEAR(t.date) = :year")
    List<Transaction> findByUserIdAndYear(
            @Param("userId") Long userId, 
            @Param("year") int year
    );

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.date >= :startDate")
    List<Transaction> findByUserIdAndDateGreaterThanEqual(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate
    );
}
