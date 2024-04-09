package com.mytech.api.models.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    
    private int expenseId;
    private int userId;
    private int walletId;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private Integer categoryId;
    private String notes;
    private Integer recurrenceId;
}