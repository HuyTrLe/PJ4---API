package com.mytech.api.services.saving_goals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.saving_goals.EndDateType;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.expense.ExpenseRepository;
import com.mytech.api.repositories.income.IncomeRepository;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.notification.NotificationService;

@Service
public class SavingGoalsServiceImpl implements SavingGoalsService {
    @Autowired
    Saving_goalsRepository savingGoalsRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    NotificationService notificationService;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    IncomeRepository incomeRepository;

    @Autowired
    ExpenseRepository expenseRepository;

    @Override
    public List<SavingGoalDTO> getAllSavingGoals() {
        List<SavingGoal> savingGoals = savingGoalsRepository.findAll();
        return savingGoals.stream().map(goal -> modelMapper.map(goal, SavingGoalDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<SavingGoalDTO> getSavingGoalsByUserId(Long userId) {
        List<SavingGoal> savingGoals = savingGoalsRepository.findByUserId(userId);
        return savingGoals.stream().map(goal -> modelMapper.map(goal, SavingGoalDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSavingGoalById(Long savingGoalId) {
        // Lấy thông tin mục tiêu tiết kiệm cần xóa từ cơ sở dữ liệu
        SavingGoal savingGoalToDelete = savingGoalsRepository.findById(savingGoalId)
                .orElseThrow(() -> new RuntimeException("Saving Goal not found with ID: " + savingGoalId));
        // Lấy ví liên kết với mục tiêu tiết kiệm
        Wallet wallet = savingGoalToDelete.getWallet();
        // Lấy số tiền hiện tại của mục tiêu tiết kiệm
        BigDecimal currentAmount = savingGoalToDelete.getCurrentAmount();
        // Trừ số tiền hiện tại của mục tiêu tiết kiệm khỏi số dư của ví
        BigDecimal newBalance = wallet.getBalance().subtract(currentAmount);
        wallet.setBalance(newBalance);
        // Lưu cập nhật số dư của ví vào cơ sở dữ liệu
        walletRepository.save(wallet);
        // Xóa mục tiêu tiết kiệm khỏi cơ sở dữ liệu
        savingGoalsRepository.deleteById(savingGoalId);
    }

    @Override
    public boolean existsSavingGoalById(Long savingGoalId) {
        return savingGoalsRepository.existsById(savingGoalId);
    }

    @Override
    public SavingGoalDTO createSavingGoal(SavingGoalDTO savingGoalDTO) {
        SavingGoal savingGoal = modelMapper.map(savingGoalDTO, SavingGoal.class);
        Wallet wallet = walletRepository.findById(savingGoalDTO.getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (savingGoal.getTargetAmount().compareTo(savingGoal.getCurrentAmount()) <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than current amount");
        }

        // Set end date based on the type
        if (savingGoalDTO.getEndDateType() == null) {
            throw new IllegalArgumentException("You need to choose either Forever or End Date for Goals");
        } else if (EndDateType.FOREVER.equals(savingGoalDTO.getEndDateType())) {
            savingGoal.setEndDate(null);
        } else if (EndDateType.END_DATE.equals(savingGoalDTO.getEndDateType())) {
            LocalDate startDate = savingGoalDTO.getStartDate();
            LocalDate endDate = savingGoalDTO.getEndDate();

            if (endDate == null) {
                throw new IllegalArgumentException("Please select an End Date for the goal");
            }

            if (endDate.isEqual(startDate) || endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date cannot be before or in start date.");
            }

            savingGoal.setEndDate(endDate);
        }

        SavingGoal createdSavingGoal = savingGoalsRepository.save(savingGoal);

        // Update wallet balance if current amount > 0
        if (savingGoal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newBalance = wallet.getBalance().add(savingGoal.getCurrentAmount());
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
            Transaction incomeTransaction = new Transaction();
            incomeTransaction.setWallet(wallet);
            incomeTransaction.setTransactionDate(LocalDate.now());
            incomeTransaction.setAmount(savingGoal.getCurrentAmount().abs());
            incomeTransaction.setUser(wallet.getUser());
            incomeTransaction.setSavingGoal(createdSavingGoal);

            List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer",
                    savingGoalDTO.getUserId());
            if (!incomeCategories.isEmpty()) {
                Category incomeCategory = incomeCategories.get(0);
                incomeTransaction.setCategory(incomeCategory);
                incomeTransaction = transactionRepository.save(incomeTransaction);
                Income income = new Income();
                income.setAmount(savingGoal.getCurrentAmount().abs());
                income.setIncomeDate(LocalDate.now());
                income.setUser(wallet.getUser());
                income.setTransaction(incomeTransaction);
                income.setWallet(wallet);
                income.setCategory(incomeCategory);
                incomeRepository.save(income);
            }
        }

        return modelMapper.map(createdSavingGoal, SavingGoalDTO.class);
    }

    @Override
    @Transactional
    public SavingGoalDTO updateSavingGoal(Long savingGoalId, SavingGoalDTO updateSavingGoalDTO) {
        SavingGoal existingSavingGoal = savingGoalsRepository.findById(savingGoalId)
                .orElseThrow(() -> new RuntimeException("Saving Goal not found with ID: " + savingGoalId));
        Wallet wallet = walletRepository.findById(existingSavingGoal.getWallet().getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        BigDecimal oldAmount = existingSavingGoal.getCurrentAmount();
        BigDecimal newAmount = updateSavingGoalDTO.getCurrentAmount();
        BigDecimal difference = newAmount.subtract(oldAmount);
        if (updateSavingGoalDTO.getTargetAmount().compareTo(updateSavingGoalDTO.getCurrentAmount()) < 0) {
            throw new IllegalArgumentException("Target amount must be greater than current amount");
        }
        existingSavingGoal.setTargetAmount(updateSavingGoalDTO.getTargetAmount());
        existingSavingGoal.setCurrentAmount(updateSavingGoalDTO.getCurrentAmount());

        existingSavingGoal.setTargetAmount(updateSavingGoalDTO.getTargetAmount());
        existingSavingGoal.setCurrentAmount(newAmount);
        existingSavingGoal.setStartDate(updateSavingGoalDTO.getStartDate());
        EndDateType newEndDateType = updateSavingGoalDTO.getEndDateType();
        if (newEndDateType != null) {
            if (!(EndDateType.FOREVER.equals(newEndDateType) || EndDateType.END_DATE.equals(newEndDateType))) {
                throw new IllegalArgumentException("Invalid end date type.");
            }

            existingSavingGoal.setEndDateType(newEndDateType);
            if (EndDateType.END_DATE.equals(newEndDateType)) {
                LocalDate newEndDate = updateSavingGoalDTO.getEndDate();
                if (newEndDate == null) {
                    throw new IllegalArgumentException("End Date cannot be null");
                }
                if (newEndDate.isBefore(existingSavingGoal.getStartDate())
                        || newEndDate.isEqual(existingSavingGoal.getStartDate())) {
                    throw new IllegalArgumentException("End date cannot be before or equal to start date.");
                }
                existingSavingGoal.setEndDate(newEndDate);
                List<Transaction> transactions = transactionRepository.findBySavingGoal_Id(savingGoalId);
                transactions.stream()
                        .filter(t -> t.getTransactionDate().isBefore(updateSavingGoalDTO.getStartDate())
                                || (newEndDate != null && t.getTransactionDate().isAfter(newEndDate)))
                        .forEach(transaction -> {
                            if (transaction.getCategory().getType() == CateTypeENum.INCOME) {
                                wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
                                existingSavingGoal.setCurrentAmount(
                                        existingSavingGoal.getCurrentAmount().subtract(transaction.getAmount()));
                            } else {
                                wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
                                existingSavingGoal
                                        .setCurrentAmount(
                                                existingSavingGoal.getCurrentAmount().add(transaction.getAmount()));
                            }
                            transactionRepository.delete(transaction);
                        });
            } else {
                existingSavingGoal.setEndDate(null);
            }
        }

        BigDecimal newWalletBalance = wallet.getBalance().add(difference);
        wallet.setBalance(newWalletBalance);
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            Transaction adjustmentTransaction = new Transaction();
            adjustmentTransaction.setWallet(wallet);
            adjustmentTransaction.setTransactionDate(LocalDate.now());
            adjustmentTransaction.setAmount(difference.abs());
            adjustmentTransaction.setUser(wallet.getUser());
            adjustmentTransaction.setSavingGoal(existingSavingGoal);
            Category category = null;
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer",
                        updateSavingGoalDTO.getUserId());
                if (!incomeCategories.isEmpty()) {
                    category = incomeCategories.get(0); // Lấy danh mục thu nhập đầu tiên
                }
            } else {
                List<Category> expenseCategories = categoryRepository.findByNameAndUserId("Outgoing Transfer",
                        updateSavingGoalDTO.getUserId());
                if (!expenseCategories.isEmpty()) {
                    category = expenseCategories.get(0); // Lấy danh mục chi tiêu đầu tiên
                }
            }

            if (category != null) {
                adjustmentTransaction.setCategory(category);
                adjustmentTransaction = transactionRepository.save(adjustmentTransaction);
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    Income income = new Income();
                    income.setAmount(difference.abs());
                    income.setIncomeDate(LocalDate.now());
                    income.setUser(wallet.getUser());
                    income.setTransaction(adjustmentTransaction);
                    income.setWallet(wallet);
                    income.setCategory(category);
                    incomeRepository.save(income);
                } else {
                    Expense expense = new Expense();
                    expense.setAmount(difference.abs());
                    expense.setExpenseDate(LocalDate.now());
                    expense.setUser(wallet.getUser());
                    expense.setTransaction(adjustmentTransaction);
                    expense.setWallet(wallet);
                    expense.setCategory(category);
                    expenseRepository.save(expense);
                }
            }
        }
        savingGoalsRepository.save(existingSavingGoal);
        walletRepository.save(wallet);
        return modelMapper.map(existingSavingGoal, SavingGoalDTO.class);
    }

    @Override
    public SavingGoalDTO getSavingGoalById(Long savingGoalId) {
        Optional<SavingGoal> savingGoalOptional = savingGoalsRepository.findById(savingGoalId);
        if (savingGoalOptional.isPresent()) {
            return modelMapper.map(savingGoalOptional.get(), SavingGoalDTO.class);
        } else {
            throw new IllegalArgumentException("Saving Goal not found with ID: " + savingGoalId);
        }
    }

    @Override
    public List<SavingGoal> getSavingGoalsByWalletId(int userId, Integer walletId) {
        return savingGoalsRepository.findByUserIdAndWallet_WalletId(userId, walletId);
    }

}