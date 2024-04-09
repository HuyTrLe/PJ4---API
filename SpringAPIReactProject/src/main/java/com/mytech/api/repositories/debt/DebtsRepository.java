package com.mytech.api.repositories.debt;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mytech.api.models.debt.Debt;

public interface DebtsRepository extends JpaRepository<Debt, Long> {

    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId")
    List<Debt> findByUserId(Long userId);

//    @Query("SELECT COUNT(d) > 0 FROM Debt d WHERE d.id = :debtId")
//    boolean existsById(@Param("debtId") Long debtId);



    
    void deleteDebtById(Long debtId);
}