package com.mytech.api.models.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
	private Integer transactionId;
    private Integer userId;
    private Integer walletId;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private Integer categoryId;
    private String notes;
    private Integer recurrenceId;
    private String currency;
}
