package com.mytech.api.models.budget;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParamBudget {
	private int userId;
	private LocalDate fromDate;
	private LocalDate toDate;
}