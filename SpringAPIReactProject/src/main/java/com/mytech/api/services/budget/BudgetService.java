package com.mytech.api.services.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

	void adjustBudgetForTransaction(Transaction transaction, boolean isDeletion, BigDecimal oldAmount,
			LocalDate oldTransactionDate);

	Optional<Budget> findBudgetByCategoryId(Long categoryId);

	void adjustBudgetForCategory(Long categoryId, BigDecimal amount);

	Page<Budget> getValidBudget(int userId, Pageable pageable);

	Page<Budget> getPastBudgets(int userId, Pageable pageable);

	Page<Budget> getFutureBudgets(int userId, Pageable pageable);

	Budget createAndInitializeBudget(Long categoryId, BigDecimal initialAmount);

	List<BudgetResponse> getBudgetPast(ParamBudget param);

	List<BudgetResponse> getBudgetFuture(ParamBudget param);
}
