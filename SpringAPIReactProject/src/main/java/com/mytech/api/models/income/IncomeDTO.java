package com.mytech.api.models.income;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDTO {
    
    private int incomeId;
    private int userId;
    private int walletId;
    private BigDecimal amount;
    private LocalDate incomeDate;
    private Integer categoryId;
    private String notes;
    private Integer recurrenceId;
}
