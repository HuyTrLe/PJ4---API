package com.mytech.api.models.recurrence;

import java.time.DayOfWeek;
import java.time.LocalDate;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecurrenceDTO {
	private int recurrenceId;

	private int userId;

	private String frequency; // repeat daily, repeat weekly, repeat monthly, repeat yearly

	private Integer every; // number of days/weeks/months/years

	private DayOfWeek dayOfWeek; // Monday, Tuesday, ...

	private MonthOption monthOption; // cùng ngày với start date, thứ 5 tuần thứ 4

	private LocalDate dueDate;

	@NotBlank(message = "You need to choose Forever or Until or Times to repeat")
	private EndType endType; // forever, until, times

	@Future(message = "End date must be in the future")
	@Temporal(TemporalType.DATE)
	private LocalDate endDate; // if choosing until

	private Integer times; // if choosing times

	private Integer timesCompleted;

	@FutureOrPresent(message = "Start date must be in the present or future")
	@Temporal(TemporalType.DATE)
	private LocalDate startDate; // Start date: now and future
}
