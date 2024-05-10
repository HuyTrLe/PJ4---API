package com.mytech.api.repositories.debt;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mytech.api.models.debt.Debt;

public interface DebtsRepository extends JpaRepository<Debt, Long> {

    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId")
    Page<Debt> findByUserId(Long userId, Pageable pageable);

    // @Query("SELECT COUNT(d) > 0 FROM Debt d WHERE d.id = :debtId")
    // boolean existsById(@Param("debtId") Long debtId);

    void deleteDebtById(Long debtId);
    
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId and d.isPaid = false")
    List<Debt> findDebtActive(Long userId);
    
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId and d.isPaid = true")
    List<Debt> findDebtPaid(Long userId);
    

}