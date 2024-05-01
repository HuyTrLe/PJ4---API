package com.mytech.api.models.transaction;

import java.math.BigDecimal;

import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.recurrence.RecurrenceDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRecurringDTO {

    private int transactionRecurringId;
    private Long userId;
    private int walletId;

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    private CategoryDTO category;
    private String notes;
    private RecurrenceDTO recurrence;
}
