package com.mytech.api.models.recurrence;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecurrenceConverter {

    @Autowired
    ModelMapper modelMapper;

    public Recurrence convertToEntity(RecurrenceDTO recurrenceRequestDTO) {
        Recurrence recurrence = modelMapper.map(recurrenceRequestDTO, Recurrence.class);

        // Map frequency
        switch (recurrenceRequestDTO.getFrequency()) {
            case "repeat daily":
                recurrence.setFrequencyType(FrequencyType.DAILY);
                recurrence.setEvery(recurrenceRequestDTO.getEvery());
                break;
            case "repeat weekly":
                recurrence.setFrequencyType(FrequencyType.WEEKLY);
                recurrence.setDayOfWeek(recurrenceRequestDTO.getDayOfWeek());
                recurrence.setEvery(recurrenceRequestDTO.getEvery());
                break;
            case "repeat monthly":
                recurrence.setFrequencyType(FrequencyType.MONTHLY);
                recurrence.setMonthOption(MonthOption.valueOf(recurrenceRequestDTO.getMonthOption().toUpperCase()));
                recurrence.setEvery(recurrenceRequestDTO.getEvery());
                break;
            case "repeat yearly":
                recurrence.setFrequencyType(FrequencyType.YEARLY);
                recurrence.setEvery(recurrenceRequestDTO.getEvery());
                break;
        }

        // Map endType
        switch (recurrenceRequestDTO.getEndType()) {
            case "forever":
                recurrence.setEndType(EndType.FOREVER);
                break;
            case "until":
                recurrence.setEndType(EndType.UNTIL);
                recurrence.setEndDate(recurrenceRequestDTO.getEndDate());
                break;
            case "times":
                recurrence.setEndType(EndType.TIMES);
                recurrence.setTimes(recurrenceRequestDTO.getTimes());
                break;
        }
        LocalDate dueDate = calculateDueDate(recurrenceRequestDTO);
        recurrence.setDueDate(dueDate);

        if (recurrence.getFrequencyType() == FrequencyType.MONTHLY &&
                recurrence.getMonthOption() == MonthOption.DAYOFWEEKOFMONTH) {
            recurrence.setDueDate(calculateDueDateForWeekOfMonth(recurrenceRequestDTO));
        }

        return recurrence;
    }

    private LocalDate calculateDueDate(RecurrenceDTO recurrenceRequestDTO) {
        LocalDate startDate = recurrenceRequestDTO.getStartDate();
        String frequency = recurrenceRequestDTO.getFrequency();

        switch (frequency) {
            case "repeat daily":
                return startDate.plusDays(recurrenceRequestDTO.getEvery());
            case "repeat weekly":
                DayOfWeek selectedDayOfWeek = recurrenceRequestDTO.getDayOfWeek();
                LocalDate nextDueDate = startDate.plusWeeks(recurrenceRequestDTO.getEvery());
                int daysToAdd = selectedDayOfWeek.getValue() - startDate.getDayOfWeek().getValue();
                if (daysToAdd < 0) {
                    daysToAdd += 7;
                }
                nextDueDate = nextDueDate.plusDays(daysToAdd);
                return nextDueDate;
            case "repeat monthly":
                if ("SAMEDAY".equals(recurrenceRequestDTO.getMonthOption())) {
                    return startDate.plusMonths(recurrenceRequestDTO.getEvery());
                } else if ("DAYOFWEEKOFMONTH".equals(recurrenceRequestDTO.getMonthOption())) {
                    return calculateDueDateForWeekOfMonth(recurrenceRequestDTO);
                }
                break;
            case "repeat yearly":
                return startDate.plusYears(recurrenceRequestDTO.getEvery());
        }
        return null;
    }

    private LocalDate calculateDueDateForWeekOfMonth(RecurrenceDTO recurrenceRequestDTO) {
        LocalDate startDate = recurrenceRequestDTO.getStartDate();
        int weekOfMonth = (startDate.getDayOfMonth() - 1) / 7 + 1;
        int every = recurrenceRequestDTO.getEvery();

        LocalDate nextMonthDate = startDate.plusMonths(every);
        LocalDate firstDayOfNextMonth = nextMonthDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate dayOfWeekInMonth = firstDayOfNextMonth.plusDays((weekOfMonth - 1) * 7);

        DayOfWeek desiredDayOfWeek = startDate.getDayOfWeek();
        while (dayOfWeekInMonth.getDayOfWeek() != desiredDayOfWeek) {
            dayOfWeekInMonth = dayOfWeekInMonth.plusDays(1);
        }

        return dayOfWeekInMonth;
    }

}
