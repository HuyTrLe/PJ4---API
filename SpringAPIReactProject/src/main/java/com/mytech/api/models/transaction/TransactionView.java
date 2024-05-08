package com.mytech.api.models.transaction;


import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionView {
	private String categoryName;
	private BigDecimal amount;
	private String cateIcon;
	private LocalDate transactionDate;
}
