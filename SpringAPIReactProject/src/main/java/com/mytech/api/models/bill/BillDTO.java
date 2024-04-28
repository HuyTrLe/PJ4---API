package com.mytech.api.models.bill;

import java.math.BigDecimal;

import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.models.wallet.WalletDTO;

import jakarta.validation.constraints.DecimalMin;
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

	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	@NotNull(message = "Amount must not be null")
	private BigDecimal amount;

	private RecurrenceDTO recurrence;

	private CategoryDTO category;

	private WalletDTO wallet;

	public BillDTO(int billId, UserDTO user, BigDecimal amount,
			RecurrenceDTO recurrence, CategoryDTO category, WalletDTO wallet) {
		this.billId = billId;
		this.user = user;
		this.amount = amount;
		this.recurrence = recurrence;
		this.category = category;
		this.wallet = wallet;
	}
}