package com.mytech.api.models.saving_goals;




import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionWithSaving {
	private int userId;
	private Long goalId;
}