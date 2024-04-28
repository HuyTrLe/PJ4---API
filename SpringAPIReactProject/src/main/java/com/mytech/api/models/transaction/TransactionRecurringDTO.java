package com.mytech.api.models.transaction;

import java.math.BigDecimal;

import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.models.wallet.WalletDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRecurringDTO {
    private int transactionRecurringId;
    private UserDTO user;
    private WalletDTO wallet;
    private BigDecimal amount;
    private CategoryDTO category;
    private String notes;
    private RecurrenceDTO recurrence;
}
