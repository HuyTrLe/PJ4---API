package com.mytech.api.repositories.budget;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.budget.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
	List<Budget> findByUserId(int userId);
	
    Budget findByUserIdAndCategory_Id(Long userId, Long categoryId);
}
