package com.mytech.api.models.transaction;


import java.math.BigDecimal;

import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.recurrence.RecurrenceDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionResponce {
	private int transactionRecurringId;

	private Long userId;

	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	@NotNull(message = "Amount must not be null")
	private BigDecimal amount;

	private RecurrenceDTO recurrence;

	private CategoryDTO category;

	private int walletId;

	public TransactionResponce(int transactionRecurringId, Long userId, BigDecimal amount,
			RecurrenceDTO recurrence, CategoryDTO category, int walletId) {
		this.transactionRecurringId = transactionRecurringId;
		this.userId = userId;
		this.amount = amount;
		this.recurrence = recurrence;
		this.category = category;
		this.walletId = walletId;
	}
}