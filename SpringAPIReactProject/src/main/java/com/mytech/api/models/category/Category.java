package com.mytech.api.models.category;

import java.util.ArrayList;
import java.util.List;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CateTypeENum type;

	@ManyToOne
	@JoinColumn(name = "icon_id")
	private Cat_Icon icon;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Transaction> transaction = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Budget> budget = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Bill> bill = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Debt> debt = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TransactionRecurring> transactionRecurrings = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Expense> expenses = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Income> incomes = new ArrayList<>();

	public Category(String name, CateTypeENum type, Cat_Icon icon, User user) {
		this.name = name;
		this.type = type;
		this.icon = icon;
		this.user = user;
	}
}
