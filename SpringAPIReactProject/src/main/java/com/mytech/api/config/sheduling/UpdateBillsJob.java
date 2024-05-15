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
import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.recurrence.MonthOption;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;
import com.mytech.api.services.bill.BillService;
import com.mytech.api.services.transaction.TransactionRecurringService;
import com.mytech.api.services.transaction.TransactionService;

import jakarta.transaction.Transactional;

@Component
public class UpdateBillsJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(UpdateBillsJob.class);

    @Autowired
    private EmailNotification emailService;

    @Autowired
    private BillService billService;

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
        List<Bill> billsDueToday = billService.findByRecurrence_DueDate(currentDate);

        for (Bill bill : billsDueToday) {
            Recurrence recurrence = bill.getRecurrence();
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
                    transaction.setUser(bill.getUser());
                    transaction.setAmount(bill.getAmount());
                    transaction.setCategory(bill.getCategory());
                    transaction.setWallet(bill.getWallet());
                    transaction.setTransactionDate(currentDate);
                    transaction.setNotes("Pay Bills");

                    switch (bill.getCategory().getType()) {
                        case INCOME:
                            Income income = new Income();
                            income.setUser(bill.getUser());
                            income.setWallet(bill.getWallet());
                            income.setAmount(bill.getAmount());
                            income.setIncomeDate(currentDate);
                            income.setCategory(bill.getCategory());
                            income.setTransaction(transaction);
                            transaction.setIncome(income);
                            break;
                        case EXPENSE:
                            Expense expense = new Expense();
                            expense.setUser(bill.getUser());
                            expense.setWallet(bill.getWallet());
                            expense.setAmount(bill.getAmount());
                            expense.setExpenseDate(currentDate);
                            expense.setCategory(bill.getCategory());
                            expense.setTransaction(transaction);
                            transaction.setExpense(expense);
                        default:
                            break;
                    }
                    transactionService.saveTransaction(transaction);
                    String emailContent = buildEmailContent(transaction);
                    emailService.send(bill.getUser().getEmail(), emailContent);
                    logger.info("Updated bill ID: {} with new due date: {}", bill.getBillId(), nextDueDate);
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
        return "Dear " + transaction.getUser().getUsername() + ",\n\n" +
                "Your payment for the bill '" + transaction.getCategory().getName()
                + "' has been processed successfully. " +
                "Amount: " + transaction.getAmount() + "\n" +
                "Date: " + transaction.getTransactionDate() + "\n\n" +
                "Thank you for using our services!\n" +
                "MyTech Support Team";
    }

}
