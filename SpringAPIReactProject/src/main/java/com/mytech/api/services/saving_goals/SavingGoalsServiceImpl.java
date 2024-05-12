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

import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
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
        } else if ("FOREVER".equals(savingGoalDTO.getEndDateType())) {
            savingGoal.setEndDate(null);
        } else if ("END_DATE".equals(savingGoalDTO.getEndDateType())) {
            if (savingGoalDTO.getEndDate() == null) {
                throw new IllegalArgumentException("Please select an End Date for the goal");
            }
            savingGoal.setEndDate(savingGoalDTO.getEndDate());
        }

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

            List<Category> incomeCategories = categoryRepository.findByName("Incoming Transfer");
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

        SavingGoal createdSavingGoal = savingGoalsRepository.save(savingGoal);
        return modelMapper.map(createdSavingGoal, SavingGoalDTO.class);
    }

    @Override
    @Transactional
    public SavingGoalDTO updateSavingGoal(Long savingGoalId, SavingGoalDTO updateSavingGoalDTO) {
        // Lấy mục tiêu tiết kiệm hiện có từ cơ sở dữ liệu
        SavingGoal existingSavingGoal = savingGoalsRepository.findById(savingGoalId)
                .orElseThrow(() -> new RuntimeException("Saving Goal not found with ID: " + savingGoalId));

        // Lấy ví liên kết với mục tiêu tiết kiệm
        Wallet wallet = walletRepository.findById(existingSavingGoal.getWallet().getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Lấy số tiền hiện tại của mục tiêu tiết kiệm trước khi cập nhật
        BigDecimal oldAmount = existingSavingGoal.getCurrentAmount();

        // Lấy số tiền mới từ DTO cập nhật
        BigDecimal newAmount = updateSavingGoalDTO.getCurrentAmount();

        // Tính toán sự khác biệt giữa số tiền mới và số tiền cũ
        BigDecimal difference = newAmount.subtract(oldAmount);

        // Cập nhật số tiền mới cho mục tiêu tiết kiệm và cập nhật số dư của ví
        existingSavingGoal.setCurrentAmount(newAmount);
        BigDecimal newWalletBalance = wallet.getBalance().add(difference);
        wallet.setBalance(newWalletBalance);

        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            Transaction adjustmentTransaction = new Transaction();
            adjustmentTransaction.setWallet(wallet);
            adjustmentTransaction.setTransactionDate(LocalDate.now());
            adjustmentTransaction.setAmount(difference.abs()); // Số tiền điều chỉnh là giá trị tuyệt đối của sự khác
                                                               // biệt
            adjustmentTransaction.setUser(wallet.getUser());

            // Xác định danh mục dựa trên dấu của sự khác biệt
            Category category = null;
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                // Tìm danh mục thu nhập
                List<Category> incomeCategories = categoryRepository.findByName("Incoming Transfer");
                if (!incomeCategories.isEmpty()) {
                    category = incomeCategories.get(0); // Lấy danh mục thu nhập đầu tiên
                }
            } else {
                // Tìm danh mục chi tiêu
                List<Category> expenseCategories = categoryRepository.findByName("Outgoing Transfer");
                if (!expenseCategories.isEmpty()) {
                    category = expenseCategories.get(0); // Lấy danh mục chi tiêu đầu tiên
                }
            }

            if (category != null) {
                // Gán danh mục cho giao dịch điều chỉnh
                adjustmentTransaction.setCategory(category);

                // Lưu giao dịch điều chỉnh vào cơ sở dữ liệu
                adjustmentTransaction = transactionRepository.save(adjustmentTransaction);

                // Tùy thuộc vào dấu của sự khác biệt, tạo giao dịch thu nhập hoặc chi tiêu
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    // Tạo giao dịch thu nhập
                    Income income = new Income();
                    income.setAmount(difference.abs()); // Số tiền thu nhập là giá trị tuyệt đối của sự khác biệt
                    income.setIncomeDate(LocalDate.now());
                    income.setUser(wallet.getUser());
                    income.setTransaction(adjustmentTransaction);
                    income.setWallet(wallet);
                    income.setCategory(category);

                    // Lưu giao dịch thu nhập vào cơ sở dữ liệu
                    incomeRepository.save(income);
                } else {
                    // Tạo giao dịch chi tiêu
                    Expense expense = new Expense();
                    expense.setAmount(difference.abs()); // Số tiền chi tiêu là giá trị tuyệt đối của sự khác biệt
                    expense.setExpenseDate(LocalDate.now());
                    expense.setUser(wallet.getUser());
                    expense.setTransaction(adjustmentTransaction);
                    expense.setWallet(wallet);
                    expense.setCategory(category);
                    // Lưu giao dịch chi tiêu vào cơ sở dữ liệu
                    expenseRepository.save(expense);
                }
            }
        }
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current amount cannot be negative.");
        }
        // Lưu mục tiêu tiết kiệm và cập nhật số dư của ví vào cơ sở dữ liệu
        savingGoalsRepository.save(existingSavingGoal);
        walletRepository.save(wallet);
        // Trả về DTO của mục tiêu tiết kiệm đã được cập nhật
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