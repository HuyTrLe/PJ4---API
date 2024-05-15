package com.mytech.api.models.saving_goals;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingParam {
	private Long userId;
	private LocalDate fromDate;
	private LocalDate toDate;
}