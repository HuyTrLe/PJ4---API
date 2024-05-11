package com.mytech.api.repositories.debt;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.debt.ReportDebt;

public interface DebtsRepository extends JpaRepository<Debt, Long> {

    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId")
    Page<Debt> findByUserId(Long userId, Pageable pageable);

    // @Query("SELECT COUNT(d) > 0 FROM Debt d WHERE d.id = :debtId")
    // boolean existsById(@Param("debtId") Long debtId);

    void deleteDebtById(Long debtId);
    
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId and d.dueDate >= :currentDate")
    List<Debt> findDebtValid(Long userId, LocalDate currentDate);
    
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId and d.isPaid = false")
    List<Debt> findDebtActive(Long userId);
    
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId and d.isPaid = true")
    List<Debt> findDebtPaid(Long userId);
    
    @Query("SELECT d FROM Debt d "
    		+ "JOIN d.category c "
    		+ "WHERE d.user.id = :userId and c.name = 'Debt'")
    List<Debt> findDebt(Long userId);
    
    
    @Query("SELECT d FROM Debt d "
    		+ "JOIN d.category c "
    		+ "WHERE d.user.id = :userId and c.name = 'Loan'")
    List<Debt> findLoan(Long userId);
    
    //Report
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Paid before due',COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Debt' AND d.isPaid = true AND d.paidDate <= d.dueDate")
    ReportDebt GetDebtTTH(Long userId);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Paid after due', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Debt' AND d.isPaid = true AND d.paidDate > d.dueDate")
    ReportDebt GetDebtTSTH(Long userId);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Not pay before due', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Debt' AND d.isPaid = false AND d.dueDate >= :currentDate")
    ReportDebt GetDebtCTCTTH(Long userId,LocalDate currentDate);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Not pay overdue', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Debt' AND d.isPaid = false AND d.dueDate < :currentDate")
    ReportDebt GetDebtCTQTH(Long userId,LocalDate currentDate);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Received before due', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Loan' AND d.isPaid = true AND d.paidDate <= d.dueDate")
    ReportDebt GetLoanTTH(Long userId);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Received after due', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Loan' AND d.isPaid = true AND d.paidDate > d.dueDate")
    ReportDebt GetLoanTSTH(Long userId);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Not receive before due', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Loan' AND d.isPaid = false AND d.dueDate >= :currentDate")
    ReportDebt GetLoanCTCTTH(Long userId,LocalDate currentDate);
    
    @Query("SELECT new com.mytech.api.models.debt.ReportDebt('Not receive overdue', COUNT(d)) "
    		+ "FROM Debt d WHERE d.user.id = :userId and d.category.name = 'Loan' AND d.isPaid = false AND d.dueDate < :currentDate")
    ReportDebt GetLoanCTQTH(Long userId,LocalDate currentDate);
    
    //
    

}