package com.mytech.api.models.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private int expenseId;
    private int userId;
    private int walletId;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private int categoryId;
    private String notes;
}