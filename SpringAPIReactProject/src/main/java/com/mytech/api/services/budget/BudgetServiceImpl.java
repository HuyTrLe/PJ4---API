package com.mytech.api.services.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.models.notifications.NotificationType;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.budget.BudgetRepository;
import com.mytech.api.services.notification.NotificationService;

@Service
public class BudgetServiceImpl implements BudgetService {

	private final BudgetRepository budgetRepository;
	
	private final NotificationService notificationService;

	public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService) {
		this.budgetRepository = budgetRepository;
		this.notificationService = notificationService;
	}

	@Override
    public Budget saveBudget(Budget budget) {
        Budget savedBudget = budgetRepository.save(budget);

        // Check if the budget reaches its limit
        if (savedBudget.getAmount().compareTo(savedBudget.getThreshold_amount()) >= 0) {
            // Create a notification DTO
            NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setUserId(savedBudget.getUser().getId());
            notificationDTO.setNotificationType(NotificationType.BUDGET_LIMIT);
            notificationDTO.setEventId(Long.valueOf(savedBudget.getBudgetId()));
            notificationDTO.setMessage("Your budget for " + savedBudget.getCategory().getName() + " has reached its limit.");
            notificationDTO.setTimestamp(LocalDateTime.now());

            // Send the notification
            notificationService.sendNotification(notificationDTO);
        }
        
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), savedBudget.getPeriod_end());
        if (daysUntilDue <= 3) {
        	NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setUserId(savedBudget.getUser().getId());
            notificationDTO.setNotificationType(NotificationType.BUDGET_DUE);
            notificationDTO.setEventId(Long.valueOf(savedBudget.getBudgetId()));
            notificationDTO.setMessage("Your budget for " + savedBudget.getCategory().getName() + " has about to due in 3 days or less.");
            notificationDTO.setTimestamp(LocalDateTime.now());

            notificationService.sendNotification(notificationDTO);
        }

        return savedBudget;
    }
	
	//@Scheduled(fixedDelayString = "PT1H") // This will run the task every hour
    public void checkBudgetsPeriodically() {
        List<Budget> allBudgets = budgetRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Budget budget : allBudgets) {
            // Check if the budget reaches its limit
            if (budget.getAmount().compareTo(budget.getThreshold_amount()) >= 0) {
                sendLimitNotification(budget);
            }

            // Check if the budget is about to be due
            long daysUntilDue = ChronoUnit.DAYS.between(today, budget.getPeriod_end());
            if (daysUntilDue <= 3) {
                sendDueNotification(budget);
            }
        }
    }

    private void sendLimitNotification(Budget budget) {
        // Create and send a notification for budget limit
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(budget.getUser().getId());
        notificationDTO.setNotificationType(NotificationType.BUDGET_LIMIT);
        notificationDTO.setEventId(Long.valueOf(budget.getBudgetId()));
        notificationDTO.setMessage("Your budget for " + budget.getCategory().getName() + " has reached its limit.");
        notificationDTO.setTimestamp(LocalDateTime.now());
        notificationService.sendNotification(notificationDTO);
    }

    private void sendDueNotification(Budget budget) {
        // Create and send a notification for budget due
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(budget.getUser().getId());
        notificationDTO.setNotificationType(NotificationType.BUDGET_DUE);
        notificationDTO.setEventId(Long.valueOf(budget.getBudgetId()));
        notificationDTO.setMessage("Your budget for " + budget.getCategory().getName() + " is about to be due in 3 days or less.");
        notificationDTO.setTimestamp(LocalDateTime.now());
        notificationService.sendNotification(notificationDTO);
    }

	@Override
	public Budget getBudgetById(int budgetId) {
		return budgetRepository.findById(budgetId).orElse(null);
	}

	@Override
	public List<Budget> getBudgetsByUserId(int userId) {
		return budgetRepository.findByUserId(userId);
	}

	@Override
	public void deleteBudget(int budgetId) {
		if (budgetRepository.existsById(budgetId)) {
			budgetRepository.deleteById(budgetId);
		} else {
			throw new RuntimeException("Expense not found with id: " + budgetId);
		}
	}

	@Override
    public void adjustBudgetForTransaction(Transaction transaction, boolean isDeletion, Transaction oldTransaction) {
        Long categoryId = transaction.getCategory().getId();
        Long userId = transaction.getUser().getId();
        Budget budget = budgetRepository.findByUserIdAndCategory_Id(userId, categoryId);

        if (budget != null) {
            BigDecimal amountChange = transaction.getAmount();
            if (isDeletion) {
                // If deleting, subtract the transaction amount from the budget
                amountChange = amountChange.negate();
            } else if (oldTransaction != null) {
                // If editing, calculate the difference between new and old amounts
                amountChange = transaction.getAmount().subtract(oldTransaction.getAmount());
            }

            // Update the budget amount based on the transaction type
            if (transaction.getExpense() != null) {
                budget.setAmount(budget.getAmount().add(amountChange));
            }

            // Save the updated budget
            saveBudget(budget);
        }
    }

}
