package com.mytech.api.repositories.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.budget.BudgetResponse;
import com.mytech.api.models.budget.ParamPudget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
	List<Budget> findByUserId(int userId);
	
    Budget findByUserIdAndCategory_Id(Long userId, Long categoryId);
    
    Optional<Budget> findByCategoryId(Long categoryId);
    
    @Query("SELECT t FROM Budget t WHERE t.user.id = :userId and t.period_start <= :toDate and t.period_end >= :fromDate")
    List<BudgetResponse> getBudgetWithTime(int userId, LocalDate fromDate, LocalDate toDate);
}
