
package com.mytech.api.models.debt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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