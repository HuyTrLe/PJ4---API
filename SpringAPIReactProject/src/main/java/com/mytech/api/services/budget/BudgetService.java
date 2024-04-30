package com.mytech.api.services.budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.transaction.Transaction;


public interface BudgetService {

	Budget saveBudget(Budget budget);

	Budget getBudgetById(int budgetId);

	List<Budget> getBudgetsByUserId(int userId);

	void deleteBudget(int budgetId);
	
	void adjustBudgetForTransaction(Transaction transaction, boolean isDeletion, BigDecimal oldAmount);
	
	Optional<Budget> findBudgetByCategoryId(Long categoryId);
	
	void adjustBudgetForCategory(Long categoryId, BigDecimal amount);
}
