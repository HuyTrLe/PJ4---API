package com.mytech.api.models.budget;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse extends Budget{

	private int CategoryName;
	private int CategoryIcon;

}