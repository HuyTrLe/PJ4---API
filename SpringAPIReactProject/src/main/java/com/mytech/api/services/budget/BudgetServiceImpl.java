package com.mytech.api.services.budget;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.repositories.budget.BudgetRepository;


@Service
public class BudgetServiceImpl implements BudgetService{
	
private final BudgetRepository budgetRepository;
	
	public BudgetServiceImpl(BudgetRepository budgetRepository) {
		this.budgetRepository = budgetRepository;
	}

	@Override
	public Budget saveBudget(Budget budget) {
		return budgetRepository.save(budget);
	}

	@Override
	public Budget getBudgetById(int budgetId) {
		return budgetRepository.findById(budgetId).orElse(null);
	}

	@Override
	public List<Budget> getBudgetsByUserId(int userId) {
		return budgetRepository.findByUserId(userId);
	}

	@Override
	public void deleteBudget(int budgetId) {
		if (budgetRepository.existsById(budgetId)) {
            budgetRepository.deleteById(budgetId);
        } else {
            throw new RuntimeException("Expense not found with id: " + budgetId);
        }
	}

}
