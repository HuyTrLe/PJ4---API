package com.mytech.api.services.expense;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.expense.Expense;
import com.mytech.api.repositories.expense.ExpenseRepository;

@Service
public class ExpenseServiceImpl implements ExpenseService{
	
	private final ExpenseRepository expenseRepository;
	
	public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
		this.expenseRepository = expenseRepository;
	}

	@Override
	public Expense saveExpense(Expense expense) {
		return expenseRepository.save(expense);
	}

	@Override
	public Expense getExpenseById(int expenseId) {
		return expenseRepository.findById(expenseId).orElse(null);
	}

	@Override
	public List<Expense> getExpensesByUserId(int userId) {
		return expenseRepository.findByUserId(userId);
	}

	@Override
	public void deleteExpense(int expenseId) {
		if (expenseRepository.existsById(expenseId)) {
            expenseRepository.deleteById(expenseId);
        } else {
            throw new RuntimeException("Expense not found with id: " + expenseId);
        }
	}

}
