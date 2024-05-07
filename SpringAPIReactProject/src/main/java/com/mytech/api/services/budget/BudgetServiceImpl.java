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
import com.mytech.api.models.budget.BudgetResponse;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.category.Category;
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
		boolean isNewBudget = budget.getBudgetId() == 0;

		// Save budget to the repository (new or updated)
		Budget savedBudget = budgetRepository.save(budget);

		// If it's a new budget, calculate sum of past transactions in the category that
		// are
		// within the budget's period, then update the budget amount.
		if (isNewBudget) {
			// Fetch all past transactions of the budget's category within the defined
			// period
			List<Transaction> pastTransactions = transactionRepository.findByCategory_IdAndTransactionDateBetween(
					savedBudget.getCategory().getId(), savedBudget.getPeriodStart(), savedBudget.getPeriodEnd());

			BigDecimal pastTransactionsSum = pastTransactions.stream().map(Transaction::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			// Update the budget's amount with the sum of past transactions
			savedBudget.setAmount(pastTransactionsSum);

			// Save the updated budget to the repository again with the new amount
			savedBudget = budgetRepository.save(savedBudget);
		}

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
	public void adjustBudgetForTransaction(Transaction transaction, boolean isDeletion, BigDecimal oldAmount,
	        LocalDate oldTransactionDate) {
	    Long categoryId = transaction.getCategory().getId();
	    Long userId = transaction.getUser().getId();
	    LocalDate newTransactionDate = transaction.getTransactionDate();

	    // Case 1: This is an update, and the transaction date has changed, or it's a new transaction.
	    if (!isDeletion) {
	        if (!oldTransactionDate.equals(newTransactionDate)) {
	            // Subtract the old amount from the budget associated with the old transaction date
	            adjustBudget(userId, categoryId, oldTransactionDate, oldAmount.negate());

	            // Add the transaction amount to the new budget date
	            adjustBudget(userId, categoryId, newTransactionDate, transaction.getAmount());
	        } else {
	            // Case 2: This is an update without change in transaction date, just adjust with the new amount.
	            BigDecimal amountDifference = transaction.getAmount().subtract(oldAmount);
	            System.out.println("amountDifference: " + amountDifference);
	            adjustBudget(userId, categoryId, newTransactionDate, amountDifference);
	        }
	    } 

	    // Case 3: If the transaction has been deleted, subtract its amount from the budget.
	    if (isDeletion) {
	        adjustBudget(userId, categoryId, oldTransactionDate, oldAmount.negate());
	    }
	}

	private void adjustBudget(Long userId, Long categoryId, LocalDate transactionDate, BigDecimal amountAdjustment) {
	    budgetRepository.findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
	    userId, categoryId, transactionDate, transactionDate
	    ).ifPresent(budget -> {
	        budget.setAmount(budget.getAmount().add(amountAdjustment));
	        budgetRepository.save(budget);
	    });
	}


	@Override
	public void adjustBudgetForCategory(Long categoryId, BigDecimal amount) {
		Optional<Budget> budgetOpt = budgetRepository.findByCategoryId(categoryId);
		if (budgetOpt.isPresent()) {
			Budget budget = budgetOpt.get();
			budget.setAmount(budget.getAmount().add(amount));
			budgetRepository.save(budget);
		} else {
			System.out.println("No budget found for category id: " + categoryId + ", skipping budget adjustment.");
		}
	}

	@Override
	public Optional<Budget> findBudgetByCategoryId(Long categoryId) {
		return budgetRepository.findByCategoryId(categoryId);
	}

	@Override
	public Budget createAndInitializeBudget(Long categoryId, BigDecimal initialAmount) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

		BigDecimal transactionSum = transactionRepository.findByCategory_Id(categoryId).stream()
				.map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		Budget newBudget = new Budget();
		newBudget.setCategory(category);
		newBudget.setAmount(initialAmount.add(transactionSum));

		LocalDate today = LocalDate.now();
		newBudget.setPeriodStart(today.with(TemporalAdjusters.firstDayOfMonth()));
		newBudget.setPeriodEnd(today.with(TemporalAdjusters.lastDayOfMonth()));

		return budgetRepository.save(newBudget);
	}

	@Override
	public List<BudgetResponse> getBudgetWithTime(ParamBudget param) {
		return budgetRepository.getBudgetWithTime(param.getUserId(), param.getFromDate(), param.getToDate());
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
