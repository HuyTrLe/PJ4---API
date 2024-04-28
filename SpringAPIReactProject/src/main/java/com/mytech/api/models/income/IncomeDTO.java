package com.mytech.api.models.income;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
