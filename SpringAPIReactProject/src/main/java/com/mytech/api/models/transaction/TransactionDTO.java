package com.mytech.api.models.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.models.wallet.WalletDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDTO {
    private Integer transactionId;
    private UserDTO user;
    private WalletDTO wallet;

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    private LocalDate transactionDate;

    private CategoryDTO category;

    private String notes;

    public TransactionDTO(Integer transactionId, UserDTO user, WalletDTO wallet, BigDecimal amount,
            LocalDate transactionDate, CategoryDTO category, String notes) {
        this.transactionId = transactionId;
        this.user = user;
        this.wallet = wallet;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.category = category;
        this.notes = notes;
    }
}
