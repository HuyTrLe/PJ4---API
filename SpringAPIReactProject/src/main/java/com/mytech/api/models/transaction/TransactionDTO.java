package com.mytech.api.models.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransactionDTO {
    private Integer transactionId;
    private Long userId;
    private int walletId;

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    private LocalDate transactionDate;

    private Long categoryId;

    private String notes;

    public TransactionDTO(Integer transactionId, Long userId, int walletId, BigDecimal amount,
            LocalDate transactionDate, long categoryId, String notes) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.walletId = walletId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.categoryId = categoryId;
        this.notes = notes;
    }

}
