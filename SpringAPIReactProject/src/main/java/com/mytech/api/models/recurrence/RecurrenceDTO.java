package com.mytech.api.models.recurrence;

import java.time.LocalDate;
import com.mytech.api.models.user.UserDTO;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
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
	private LocalDate startDate;

    @Future(message = "End date must be in the future")
	@Temporal(TemporalType.DATE)
	private LocalDate endDate;

    @Min(value = 1, message = "Interval amount must be at least 1")
    private Integer intervalAmount;

	public RecurrenceDTO(int recurrenceId, UserDTO user, LocalDate startDate, LocalDate endDate, Integer intervalAmount,
			RecurrenceType recurrenceType) {
		this.recurrenceId = recurrenceId;
		this.user = user;
		this.startDate = startDate;
		this.endDate = endDate;
		this.intervalAmount = intervalAmount;
		this.recurrenceType = recurrenceType;
	}

}
