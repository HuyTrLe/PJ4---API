package com.mytech.api.services.expense;

import java.util.List;

import com.mytech.api.models.expense.Expense;

public interface ExpenseService {
	Expense saveExpense(Expense expense);

    Expense getExpenseById(int expenseId);

    List<Expense> getExpensesByUserId(int userId);

    void deleteExpense(int expenseId);
}
