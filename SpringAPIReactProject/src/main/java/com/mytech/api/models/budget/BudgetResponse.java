package com.mytech.api.models.budget;


import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse{

	  private BigDecimal amount;
	  private BigDecimal thresholdAmount;
	  private String categoryName;       
	  private String categoryIcon; 
}