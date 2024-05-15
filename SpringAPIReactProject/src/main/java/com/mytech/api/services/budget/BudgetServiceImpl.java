package com.mytech.api.services.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.budget.BudgetResponse;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.notifications.Notification;
import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.models.notifications.NotificationType;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.budget.BudgetRepository;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.notification.NotificationsRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.services.notification.NotificationService;

@Service
public class BudgetServiceImpl implements BudgetService {

	private final BudgetRepository budgetRepository;
	private final CategoryRepository categoryRepository;
	private final TransactionRepository transactionRepository;
	private final NotificationService notificationService;
	private final NotificationsRepository notificationsRepository;

	public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService,
			CategoryRepository categoryRepository, TransactionRepository transactionRepository,
			NotificationsRepository notificationsRepository) {
		this.budgetRepository = budgetRepository;
		this.notificationService = notificationService;
		this.categoryRepository = categoryRepository;
		this.transactionRepository = transactionRepository;
		this.notificationsRepository = notificationsRepository;
	}

	@Override
	public Budget saveBudget(Budget budget) {
		Integer budgetId = budget.getBudgetId() > 0 ? budget.getBudgetId() : null;
		List<Budget> overlappingBudgets = budgetRepository.findByCategoryAndPeriodOverlaps(budget.getCategory().getId(),
				budget.getPeriodStart(), budget.getPeriodEnd(), budgetId);

		if (!overlappingBudgets.isEmpty()) {
			throw new IllegalStateException("A budget for this category and period overlaps with an existing one.");
		}

		Budget savedBudget = budgetRepository.save(budget);

		// Whether this is a new budget or updating, recalculate amounts if the period
		// has changed or if it's new
		List<Transaction> transactionsWithinPeriod = transactionRepository.findByCategory_IdAndTransactionDateBetween(
				savedBudget.getCategory().getId(), savedBudget.getPeriodStart(), savedBudget.getPeriodEnd());
		BigDecimal transactionsSum = transactionsWithinPeriod.stream().map(Transaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		savedBudget.setAmount(transactionsSum);

		savedBudget = budgetRepository.save(savedBudget);

		checkAndSendNotifications(savedBudget);
		checkAndSendNotificationsLimit(savedBudget);

		return savedBudget;
	}

	public void checkAndSendNotificationDue() {
		List<Budget> allBudgets = budgetRepository.findAll();
		for (Budget budget : allBudgets) {
			checkAndSendNotifications(budget);
		}
	}

	private void checkAndSendNotifications(Budget budget) {
		// Notification for budget due - check if already sent today
		long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), budget.getPeriodEnd());
		if (daysUntilDue <= 3) {
			Notification lastDueNotification = notificationsRepository
					.findTopByEventIdAndNotificationTypeOrderByTimestampDesc(
							Long.valueOf(budget.getBudgetId()), NotificationType.BUDGET_DUE);

			if (lastDueNotification == null ||
					ChronoUnit.DAYS.between(lastDueNotification.getTimestamp().toLocalDate(), LocalDate.now()) >= 1) {
				sendNotification(budget, NotificationType.BUDGET_DUE,
						"Your budget for " + budget.getCategory().getName() + " is about to be due in 3 days or less.");
			}
		}
	}

	private void checkAndSendNotificationsLimit(Budget budget) {
		if (budget.getAmount().compareTo(budget.getThreshold_amount()) >= 0) {
			sendNotification(budget, NotificationType.BUDGET_LIMIT,
					"Uh oh, your budget for " + budget.getCategory().getName() + " has reached its limit.");
		}
	}

	private void sendNotification(Budget budget, NotificationType type, String message) {
		// Notification existingNotification =
		// notificationsRepository.checkExistNotification(Long.valueOf(budget.getBudgetId()),
		// type);
		//
		// if (existingNotification != null) {
		// // A notification for this budget condition already exists, so do not send a
		// duplicate.
		// System.out.println("Existing found");
		// return;
		// }
		//

		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setUserId(budget.getUser().getId());
		notificationDTO.setNotificationType(type);
		notificationDTO.setEventId(Long.valueOf(budget.getBudgetId()));
		notificationDTO.setMessage(message);
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

		// Case 1: This is an update, and the transaction date has changed, or it's a
		// new transaction.
		if (!isDeletion) {
			if (!oldTransactionDate.equals(newTransactionDate)) {
				// Subtract the old amount from the budget associated with the old transaction
				// date
				Optional<Budget> oldBudgetOpt = budgetRepository
						.findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId,
								categoryId, oldTransactionDate, oldTransactionDate);
				oldBudgetOpt.ifPresent(oldBudget -> {
					adjustBudget(userId, categoryId, oldTransactionDate, oldAmount.negate());
					checkAndSendNotificationsLimit(oldBudget); // Send notification for the old budget
				});

				// Add the transaction amount to the new budget date
				adjustBudget(userId, categoryId, newTransactionDate, transaction.getAmount());
			} else {
				// Case 2: This is an update without change in transaction date, just adjust
				// with the new amount.
				BigDecimal amountDifference = transaction.getAmount().subtract(oldAmount);
				System.out.println("amountDifference: " + amountDifference);
				adjustBudget(userId, categoryId, newTransactionDate, amountDifference);
			}
		}

		// Case 3: If the transaction has been deleted, subtract its amount from the
		// budget.
		if (isDeletion) {
			adjustBudget(userId, categoryId, oldTransactionDate, oldAmount.negate());
		}

		// Case 4: Check if the budget period has changed, and reset the budget amount
		// if no transactions within the new period
		Optional<Budget> oldBudgetOpt = budgetRepository
				.findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId, categoryId,
						oldTransactionDate, oldTransactionDate);
		Optional<Budget> newBudgetOpt = budgetRepository
				.findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId, categoryId,
						newTransactionDate, newTransactionDate);

		if (oldBudgetOpt.isPresent() && newBudgetOpt.isPresent() && !oldBudgetOpt.get().equals(newBudgetOpt.get())) {
			Budget newBudget = newBudgetOpt.get();

			// Check if there are any transactions within the new budget period
			List<Transaction> transactionsInNewPeriod = transactionRepository
					.findByCategory_IdAndTransactionDateBetween(categoryId, newBudget.getPeriodStart(),
							newBudget.getPeriodEnd());

			if (transactionsInNewPeriod.isEmpty()) {
				// If no transactions within the new period, reset the budget amount to 0
				newBudget.setAmount(BigDecimal.ZERO);
			} else {
				// If there are transactions within the new period, recalculate the budget
				// amount
				BigDecimal newBudgetAmount = transactionsInNewPeriod.stream()
						.map(Transaction::getAmount)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				newBudget.setAmount(newBudgetAmount);
			}

			budgetRepository.save(newBudget);
			checkAndSendNotificationsLimit(newBudget);
		}

		// Optional<Budget> updatedBudgetOpt =
		// budgetRepository.findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId,
		// categoryId, newTransactionDate, newTransactionDate);
		// if (updatedBudgetOpt.isPresent()) {
		// Budget updatedBudget = updatedBudgetOpt.get();
		// checkAndSendNotificationsLimit(updatedBudget); // Send notification for the
		// updated budget
		// }
	}

	private void adjustBudget(Long userId, Long categoryId, LocalDate transactionDate, BigDecimal amountAdjustment) {
		budgetRepository.findByUserIdAndCategory_IdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId,
				categoryId, transactionDate, transactionDate).ifPresent(budget -> {
					budget.setAmount(budget.getAmount().add(amountAdjustment));
					Budget savedBudget = budgetRepository.save(budget);
					checkAndSendNotificationsLimit(savedBudget);
				});
	}

	@Override
	public void adjustBudgetForCategory(Long categoryId, BigDecimal amount) {
		Optional<Budget> budgetOpt = budgetRepository.findByCategoryId(categoryId);
		if (budgetOpt.isPresent()) {
			Budget budget = budgetOpt.get();
			budget.setAmount(budget.getAmount().add(amount));
			Budget savedBudget = budgetRepository.save(budget);
			checkAndSendNotificationsLimit(savedBudget);
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
		checkAndSendNotificationsLimit(newBudget);

		return budgetRepository.save(newBudget);
	}

	@Override
	public List<BudgetResponse> getBudgetWithTime(ParamBudget param) {
		return budgetRepository.getBudgetWithTime(param.getUserId(), param.getFromDate(), param.getToDate());
	}

	@Override
	public Page<Budget> getValidBudget(int userId, Pageable pageable) {
		LocalDate today = LocalDate.now();
		return budgetRepository.findByUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId, today,
				today, pageable);
	}

	@Override
	public Page<Budget> getPastBudgets(int userId, Pageable pageable) {
		LocalDate today = LocalDate.now();
		return budgetRepository.findByUserIdAndPeriodEndLessThan(userId, today, pageable);
	}

	@Override
	public Page<Budget> getFutureBudgets(int userId, Pageable pageable) {
		LocalDate today = LocalDate.now();
		return budgetRepository.findByUserIdAndPeriodStartGreaterThan(userId, today, pageable);
	}

	@Override
	public List<BudgetResponse> getBudgetPast(ParamBudget param) {
		return budgetRepository.getBudgetPast(param.getUserId(), param.getFromDate());
	}

	@Override
	public List<BudgetResponse> getBudgetFuture(ParamBudget param) {
		return budgetRepository.getBudgetFuture(param.getUserId(), param.getFromDate());
	}
}
