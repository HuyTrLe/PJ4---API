
package com.mytech.api.models.debt;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DebtDTO {
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;
    private Long userId;
    @NotNull(message = "You need to choose category")
    private Long categoryId;
    @NotBlank(message = "Creditor cannot be blank")
    private String creditor;
    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private Boolean isPaid;
    private String notes;
}