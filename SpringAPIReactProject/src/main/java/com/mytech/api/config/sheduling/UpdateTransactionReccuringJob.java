package com.mytech.api.config.sheduling;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.mytech.api.auth.email.EmailNotification;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.recurrence.MonthOption;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;
import com.mytech.api.services.transaction.TransactionRecurringService;
import com.mytech.api.services.transaction.TransactionService;

import jakarta.transaction.Transactional;

@Component
public class UpdateTransactionReccuringJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(UpdateTransactionReccuringJob.class);

    @Autowired
    private EmailNotification emailService;

    @Autowired
    private RecurrenceRepository recurrenceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRecurringService TransactionRecurringService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) {
        LocalDate currentDate = LocalDate.now();
        List<TransactionRecurring> transactionDueDates = TransactionRecurringService
                .findByRecurrence_DueDate(currentDate);

        for (TransactionRecurring transactionRecurring : transactionDueDates) {
            Recurrence recurrence = transactionRecurring.getRecurrence();
            if (recurrence != null) {
                RecurrenceDTO recurrenceDTO = modelMapper.map(recurrence, RecurrenceDTO.class);

                if (recurrenceDTO.getTimesCompleted() != null) {
                    recurrenceDTO.setTimesCompleted(recurrenceDTO.getTimesCompleted() + 1);
                } else {
                    recurrenceDTO.setTimesCompleted(1);
                }

                if (recurrence.getTimes() != 0 && recurrenceDTO.getTimesCompleted() != null) {
                    if (recurrenceDTO.getTimesCompleted() >= recurrence.getTimes()) {
                        continue;
                    }
                }
                if (recurrence.getEndDate() != null && currentDate.isAfter(recurrence.getEndDate())) {
                    continue;
                }

                LocalDate nextDueDate = calculateDueDate(recurrence, currentDate);
                if (nextDueDate != null) {
                    recurrence.setDueDate(nextDueDate);
                    recurrenceRepository.save(recurrence);
                    Transaction transaction = new Transaction();
                    transaction.setUser(transactionRecurring.getUser());
                    transaction.setAmount(transactionRecurring.getAmount());
                    transaction.setCategory(transactionRecurring.getCategory());
                    transaction.setWallet(transactionRecurring.getWallet());
                    transaction.setTransactionDate(currentDate);
                    transaction.setNotes("Pay Transaction Recurring");
                    switch (transactionRecurring.getCategory().getType()) {
                        case INCOME:
                            Income income = new Income();
                            income.setUser(transactionRecurring.getUser());
                            income.setWallet(transactionRecurring.getWallet());
                            income.setAmount(transactionRecurring.getAmount());
                            income.setIncomeDate(currentDate);
                            income.setCategory(transactionRecurring.getCategory());
                            income.setTransaction(transaction);
                            transaction.setIncome(income);
                            break;
                        case EXPENSE:
                            Expense expense = new Expense();
                            expense.setUser(transactionRecurring.getUser());
                            expense.setWallet(transactionRecurring.getWallet());
                            expense.setAmount(transactionRecurring.getAmount());
                            expense.setExpenseDate(currentDate);
                            expense.setCategory(transactionRecurring.getCategory());
                            expense.setTransaction(transaction);
                            transaction.setExpense(expense);
                            break;
                        default:
                            break;
                    }
                    transactionService.saveTransaction(transaction);
                    String emailContent = buildEmailContent(transaction);
                    emailService.send(transactionRecurring.getUser().getEmail(), emailContent);
                    logger.info("Updated bill ID: {} with new due date: {}",
                            transactionRecurring.getTransactionRecurringId(),
                            nextDueDate);
                }
            }
        }
    }

    private LocalDate calculateDueDate(Recurrence recurrenceRequest, LocalDate currentDate) {
        LocalDate dueDate = recurrenceRequest.getDueDate();

        switch (recurrenceRequest.getFrequencyType()) {
            case DAILY:
                return dueDate.plusDays(recurrenceRequest.getEvery());
            case WEEKLY:
                DayOfWeek selectedDayOfWeek = recurrenceRequest.getDayOfWeek();
                LocalDate nextDueDate = dueDate.plusWeeks(recurrenceRequest.getEvery());
                int daysToAdd = selectedDayOfWeek.getValue() -
                        dueDate.getDayOfWeek().getValue();
                if (daysToAdd < 0) {
                    daysToAdd += 7;
                }
                nextDueDate = nextDueDate.plusDays(daysToAdd);
                return nextDueDate;
            case MONTHLY:
                if (recurrenceRequest.getMonthOption() == MonthOption.SAMEDAY) {
                    return dueDate.plusMonths(recurrenceRequest.getEvery());
                } else if (recurrenceRequest.getMonthOption() == MonthOption.DAYOFWEEKOFMONTH) {
                    return calculateDueDateForWeekOfMonth(recurrenceRequest);
                }
                break;
            case YEARLY:
                return dueDate.plusYears(recurrenceRequest.getEvery());
            default:
                return null;
        }

        return null;
    }

    private LocalDate calculateDueDateForWeekOfMonth(Recurrence recurrenceRequest) {
        LocalDate dueDate = recurrenceRequest.getDueDate();
        int weekOfMonth = (dueDate.getDayOfMonth() - 1) / 7 + 1;
        int every = recurrenceRequest.getEvery();

        LocalDate nextMonthDate = dueDate.plusMonths(every);
        LocalDate firstDayOfNextMonth = nextMonthDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate dayOfWeekInMonth = firstDayOfNextMonth.plusDays((weekOfMonth - 1) *
                7);

        DayOfWeek desiredDayOfWeek = dueDate.getDayOfWeek();
        while (dayOfWeekInMonth.getDayOfWeek() != desiredDayOfWeek) {
            dayOfWeekInMonth = dayOfWeekInMonth.plusDays(1);
        }

        return dayOfWeekInMonth;
    }

    private String buildEmailContent(Transaction transaction) {
        return "<html>" +
                "<body>" +
                "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>" +
                "Dear <strong>" + transaction.getUser().getUsername() + "</strong>,</p>" +
                "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>" +
                "Your payment for the bill <strong>'" + transaction.getCategory().getName()
                + "'</strong> has been processed successfully." +
                "<br>Amount: <strong>" + transaction.getAmount() + "</strong>" +
                "<br>Date: <strong>" + transaction.getTransactionDate() + "</strong></p>" +
                "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>Thank you for using our services!</p>"
                +
                "<p style='font-family: Arial, sans-serif; font-size: 16px; font-weight: bold; color: #555;'>MyTech Support Team</p>"
                +
                "</body>" +
                "</html>";
    }

}
