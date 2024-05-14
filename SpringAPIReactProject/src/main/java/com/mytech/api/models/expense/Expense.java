package com.mytech.api.models.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.category.Category;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int expenseId;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(name = "expense_date", nullable = false)
	private LocalDate expenseDate;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@OneToOne
	@JoinColumn(name = "transaction_id")
	private Transaction transaction;

}