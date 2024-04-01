package com.mytech.api.models.recurrence;

import java.util.Date;

import com.mytech.api.models.user.UserDTO;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecurrenceDTO {
	private int recurrenceId;

	private UserDTO user;

	private RecurrenceType recurrenceType;

    @FutureOrPresent(message = "Start date must be in the present or future")
	@Temporal(TemporalType.DATE)
	private Date startDate;

    @Future(message = "End date must be in the future")
	@Temporal(TemporalType.DATE)
	private Date endDate;

	private Integer intervalAmount;

	public RecurrenceDTO(int recurrenceId, UserDTO user, Date startDate, Date endDate, Integer intervalAmount,
			RecurrenceType recurrenceType) {
		this.recurrenceId = recurrenceId;
		this.user = user;
		this.startDate = startDate;
		this.endDate = endDate;
		this.intervalAmount = intervalAmount;
		this.recurrenceType = recurrenceType;
	}

}
