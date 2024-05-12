package com.mytech.api.models.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.category.CateTypeENum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindTransactionParam{
	private int userId;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String type;
	private int walletId;
}
