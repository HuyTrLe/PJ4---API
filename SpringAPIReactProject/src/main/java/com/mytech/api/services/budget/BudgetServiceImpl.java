package com.mytech.api.services.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.models.notifications.NotificationType;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.budget.BudgetRepository;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.services.notification.NotificationService;

@Service
public class BudgetServiceImpl implements BudgetService {

	private final BudgetRepository budgetRepository;
	private final CategoryRepository categoryRepository;
	private final TransactionRepository transactionRepository;
	private final NotificationService notificationService;

	public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService,
			CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
		this.budgetRepository = budgetRepository;
		this.notificationService = notificationService;
		this.categoryRepository = categoryRepository;
		this.transactionRepository = transactionRepository;
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
			notificationDTO
					.setMessage("Your budget for " + savedBudget.getCategory().getName() + " has reached its limit.");
			notificationDTO.setTimestamp(LocalDateTime.now());

			// Send the notification
			notificationService.sendNotification(notificationDTO);
		}

		long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), savedBudget.getPeriodEnd());
		if (daysUntilDue <= 3) {
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setUserId(savedBudget.getUser().getId());
			notificationDTO.setNotificationType(NotificationType.BUDGET_DUE);
			notificationDTO.setEventId(Long.valueOf(savedBudget.getBudgetId()));
			notificationDTO.setMessage(
					"Your budget for " + savedBudget.getCategory().getName() + " has about to due in 3 days or less.");
			notificationDTO.setTimestamp(LocalDateTime.now());

			notificationService.sendNotification(notificationDTO);
		}

		return savedBudget;
	}

	// @Scheduled(fixedDelayString = "PT1H") // This will run the task every hour
	public void checkBudgetsPeriodically() {
		List<Budget> allBudgets = budgetRepository.findAll();
		LocalDate today = LocalDate.now();

		for (Budget budget : allBudgets) {
			// Check if the budget reaches its limit
			if (budget.getAmount().compareTo(budget.getThreshold_amount()) >= 0) {
				sendLimitNotification(budget);
			}

			// Check if the budget is about to be due
			long daysUntilDue = ChronoUnit.DAYS.between(today, budget.getPeriodEnd());
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
		notificationDTO.setMessage(
				"Your budget for " + budget.getCategory().getName() + " is about to be due in 3 days or less.");
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
	public void adjustBudgetForTransaction(Transaction transaction, boolean isDeletion, BigDecimal oldAmount) {
		Long categoryId = transaction.getCategory().getId();
		Long userId = transaction.getUser().getId();
		Budget budget = budgetRepository.findByUserIdAndCategory_Id(userId, categoryId);

		if (budget != null) {
			BigDecimal amountChange;

			// Determine the change in transaction amount
			if (isDeletion) {
				// If deleting, negate the transaction amount
				amountChange = transaction.getAmount().negate();
			} else {
				// If editing or creating, calculate the difference based on oldAmount
				// For new transactions, oldAmount should be passed as BigDecimal.ZERO
				amountChange = transaction.getAmount().subtract(oldAmount);
			}

			// Adjust the budget amount. This assumes all transactions affect the budget the
			// same way.
			// You might need to adjust this logic based on your specific rules for
			// different transaction types.
			budget.setAmount(budget.getAmount().add(amountChange));

			// Save the updated budget
			budgetRepository.save(budget);
		}
	}

	public void adjustBudgetForCategory(Long categoryId, BigDecimal amount) {
		Budget budget = budgetRepository.findByCategoryId(categoryId)
				.orElseThrow(() -> new RuntimeException("Budget not found for category id: " + categoryId));
		budget.setAmount(budget.getAmount().add(amount));
		budgetRepository.save(budget);
	}

	@Override
	public Optional<Budget> findBudgetByCategoryId(Long categoryId) {
		return budgetRepository.findByCategoryId(categoryId);
	}

	public void createAndInitializeBudget(Long categoryId, BigDecimal initialAmount) {
		// Create a new budget for the category
		Budget newBudget = new Budget();
		newBudget.setCategory(categoryRepository.findById(categoryId)
				.orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId)));
		newBudget.setAmount(initialAmount);

		// Find all transactions for the category
		List<Transaction> transactions = transactionRepository.findByCategory_Id(categoryId);

		// Calculate the sum of transaction amounts for the category
		BigDecimal transactionSum = transactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		// Update the budget with the sum of transactions
		newBudget.setAmount(newBudget.getAmount().add(transactionSum));

		// Save the new budget
		budgetRepository.save(newBudget);
	}

	@Override
	public List<Budget> getValidBudget(int userId) {
		LocalDate today = LocalDate.now();
	    LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
	    return budgetRepository.findByUserIdAndPeriodEndBetween(userId, today, lastDayOfMonth);
	}

	@Override
	public List<Budget> getNotValidBudget(int userId) {
		LocalDate today = LocalDate.now();
	    return budgetRepository.findByUserIdAndPeriodEndLessThan(userId, today);
	}
}
