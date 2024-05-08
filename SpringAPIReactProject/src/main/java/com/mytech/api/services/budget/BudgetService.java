package com.mytech.api.services.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.budget.BudgetResponse;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.transaction.Transaction;


public interface BudgetService {

	Budget saveBudget(Budget budget);

	Budget getBudgetById(int budgetId);

	List<Budget> getBudgetsByUserId(int userId);
	
	List<BudgetResponse> getBudgetWithTime(ParamBudget param);

	void deleteBudget(int budgetId);
	
	void adjustBudgetForTransaction(Transaction transaction, boolean isDeletion, BigDecimal oldAmount, LocalDate oldTransactionDate);
	
	Optional<Budget> findBudgetByCategoryId(Long categoryId);
	
	void adjustBudgetForCategory(Long categoryId, BigDecimal amount);
	
	List<Budget> getValidBudget(int userId);
	
	List<Budget> getPastBudgets(int userId);
	
	List<Budget> getFutureBudgets(int userId);
	
	Budget createAndInitializeBudget(Long categoryId, BigDecimal initialAmount);
}
