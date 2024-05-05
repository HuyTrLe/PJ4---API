package com.mytech.api.models.transaction;

import java.math.BigDecimal;

import com.mytech.api.models.category.CateTypeENum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionData{
	private int transactionID;
	private String categoryName;
	private String cateIcon;
	private BigDecimal amount;
	private CateTypeENum Type;
	private BigDecimal totalAmount;
	private Long categoryId;
}
