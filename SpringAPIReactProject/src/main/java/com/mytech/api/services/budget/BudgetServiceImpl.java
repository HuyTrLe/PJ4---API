package com.mytech.api.services.budget;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.models.notifications.NotificationType;
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

}
