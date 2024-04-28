package com.mytech.api.models.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.models.wallet.WalletDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private int expenseId;
    private UserDTO user;
    private WalletDTO wallet;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private CategoryDTO category;
    private String notes;
}