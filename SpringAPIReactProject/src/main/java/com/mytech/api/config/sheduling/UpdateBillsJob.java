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

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.recurrence.MonthOption;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.services.bill.BillService;
import com.mytech.api.services.transaction.TransactionRecurringService;

import jakarta.transaction.Transactional;

@Component
public class UpdateBillsJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(UpdateBillsJob.class);

    @Autowired
    private BillService billService;

    @Autowired
    private RecurrenceRepository recurrenceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionRecurringService TransactionRecurringService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) {
        LocalDate currentDate = LocalDate.now();
        List<Bill> billsDueToday = billService.findByRecurrence_DueDate(currentDate);
        List<TransactionRecurring> transactionDueDates = TransactionRecurringService
                .findByRecurrence_DueDate(currentDate);

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
                    if (bill.getCategory().getType().INCOME != null) {
                        Income income = new Income();
                        income.setUser(bill.getUser());
                        income.setWallet(bill.getWallet());
                        income.setAmount(bill.getAmount());
                        income.setIncomeDate(currentDate);
                        income.setCategory(bill.getCategory());
                        income.setTransaction(transaction);
                        transaction.setIncome(income);
                    } else {
                        Expense expense = new Expense();
                        expense.setUser(bill.getUser());
                        expense.setWallet(bill.getWallet());
                        expense.setAmount(bill.getAmount());
                        expense.setExpenseDate(currentDate);
                        expense.setCategory(bill.getCategory());
                        expense.setTransaction(transaction);
                        transaction.setExpense(expense);
                    }
                    transactionRepository.save(transaction);
                    logger.info("Updated bill ID: {} with new due date: {}", bill.getBillId(), nextDueDate);
                }
            }
        }

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
                    if (transactionRecurring.getCategory().getType() == CateTypeENum.INCOME) {
                        Income income = new Income();
                        income.setUser(transactionRecurring.getUser());
                        income.setWallet(transactionRecurring.getWallet());
                        income.setAmount(transactionRecurring.getAmount());
                        income.setIncomeDate(currentDate);
                        income.setCategory(transactionRecurring.getCategory());
                        income.setTransaction(transaction);
                        transaction.setIncome(income);
                    } else {
                        Expense expense = new Expense();
                        expense.setUser(transactionRecurring.getUser());
                        expense.setWallet(transactionRecurring.getWallet());
                        expense.setAmount(transactionRecurring.getAmount());
                        expense.setExpenseDate(currentDate);
                        expense.setCategory(transactionRecurring.getCategory());
                        expense.setTransaction(transaction);
                        transaction.setExpense(expense);
                    }
                    transactionRepository.save(transaction);
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

}
