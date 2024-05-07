package com.mytech.api.repositories.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.budget.BudgetResponse;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.category.Category;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
	List<Budget> findByUserId(int userId);
	
    Budget findByUserIdAndCategory_Id(Long userId, Long categoryId);
    
    Optional<Budget> findByCategoryId(Long categoryId);
    

    List<Budget> findByUserIdAndPeriodEndBetween(int userId, LocalDate startDate, LocalDate endDate);
    
    List<Budget> findByUserIdAndPeriodEndLessThan(int userId, LocalDate date);
    

    @Query("SELECT new com.mytech.api.models.budget.BudgetResponse(t.amount, t.threshold_amount, c.name, ci.path) FROM Budget t JOIN t.category c JOIN c.icon ci WHERE t.user.id = :userId and t.periodStart <= :toDate and t.periodEnd >= :fromDate")
    List<BudgetResponse> getBudgetWithTime(int userId, LocalDate fromDate, LocalDate toDate);
    
    Optional<Budget> findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
    	    Long userId, Long categoryId, LocalDate startDate, LocalDate endDate);
 
    List<Budget> findByUserIdAndCategory_Id(int userId, Long categoryId);

    @Query("SELECT b FROM Budget b WHERE b.category = :category AND NOT (b.periodEnd < :newStart OR b.periodStart > :newEnd)")
    List<Budget> findOverlappingBudgets(@Param("category") Category category, 
                                        @Param("newStart") LocalDate newStart, 
                                        @Param("newEnd") LocalDate newEnd);
}

