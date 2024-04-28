
package com.mytech.api.models.debt;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DebtDTO {
    private Long id;
    private String name;
    private Long userId;
    private Long categoryId;
    private String creditor;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private Boolean isPaid;
    private String notes;
}