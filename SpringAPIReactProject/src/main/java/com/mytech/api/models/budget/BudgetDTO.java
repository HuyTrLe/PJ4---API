package com.mytech.api.models.budget;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

	private int budgetId;
	private int userId;
	private Integer categoryId;
	private BigDecimal amount;
	private BigDecimal threshold_amount;
	private LocalDate period_start;
	private LocalDate period_end;

}
