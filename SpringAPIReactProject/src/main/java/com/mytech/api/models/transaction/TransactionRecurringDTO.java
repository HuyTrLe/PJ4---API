package com.mytech.api.models.transaction;

import java.math.BigDecimal;

import com.mytech.api.models.recurrence.RecurrenceDTO;

import jakarta.validation.constraints.DecimalMin;
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
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private Long categoryId;
    private String notes;
    private Long savingGoalId;
    private RecurrenceDTO recurrence;
}
