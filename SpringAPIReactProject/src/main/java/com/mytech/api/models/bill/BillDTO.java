package com.mytech.api.models.bill;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.user.UserDTO;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillDTO {

	private int billId;

	private UserDTO user;

	@NotBlank(message = "Bill name must not be blank")
	private String billName;

	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	@NotNull(message = "Amount must not be null")
	private BigDecimal amount;

	@NotNull(message = "Due date cannot be null")
	@Temporal(TemporalType.DATE)
	@Future(message = "Due date must be in the future")
	private LocalDate dueDate;

	private RecurrenceDTO recurrence;

	public BillDTO(int billId, UserDTO user, String billName, BigDecimal amount, LocalDate dueDate,
			RecurrenceDTO recurrence) {
		this.billId = billId;
		this.user = user;
		this.billName = billName;
		this.amount = amount;
		this.dueDate = dueDate;
		this.recurrence = recurrence;
	}
}