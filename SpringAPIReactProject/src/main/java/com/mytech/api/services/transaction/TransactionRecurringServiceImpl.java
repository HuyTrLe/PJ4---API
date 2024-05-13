package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceConverter;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.transaction.TransactionRecurringDTO;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.repositories.transaction.TransactionRecurringRepository;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.wallet.WalletService;

@Service
public class TransactionRecurringServiceImpl implements TransactionRecurringService {

    @Autowired
    TransactionRecurringRepository transactionRecurringRepository;
    @Autowired
    Saving_goalsRepository saving_goalsRepository;
    @Autowired
    CategoryService categoryService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    WalletService walletService;
    @Autowired
    RecurrenceRepository recurrenceRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    RecurrenceConverter recurrenceConverter;

    @Override
    public ResponseEntity<?> createTransaction(TransactionRecurringDTO transactionRecurringDTO) {
        TransactionRecurring transactionRecurring = modelMapper.map(transactionRecurringDTO,
                TransactionRecurring.class);

        if (transactionRecurringDTO.getUserId() == 0) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUser = userRepository.findById(transactionRecurringDTO.getUserId());
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + transactionRecurringDTO.getUserId(),
                    HttpStatus.NOT_FOUND);
        }
        Wallet existingWallet = walletService.getWalletById(transactionRecurringDTO.getWalletId());
        if (existingWallet == null) {
            return new ResponseEntity<>(
                    "Wallet not found with id: " + transactionRecurringDTO.getWalletId(),
                    HttpStatus.NOT_FOUND);
        }

        Category existingCategory = categoryService.getByCateId(transactionRecurringDTO.getCategoryId());
        if (existingCategory == null) {
            return new ResponseEntity<>("Category not found with id: " + transactionRecurringDTO.getCategoryId(),
                    HttpStatus.NOT_FOUND);
        }

        if (existingCategory.getType() == CateTypeENum.EXPENSE && "USD".equals(existingWallet.getCurrency())) {
            return new ResponseEntity<>("Expense transaction not allowed for USD wallet", HttpStatus.BAD_REQUEST);
        }
        Recurrence newRecurrence = recurrenceConverter.convertToEntity(transactionRecurringDTO.getRecurrence());
        newRecurrence.setStartDate(transactionRecurringDTO.getRecurrence().getStartDate());
        newRecurrence.setUser(existingUser.get());
        Recurrence savedRecurrence = recurrenceRepository.save(newRecurrence);
        transactionRecurring.setRecurrence(savedRecurrence);
        transactionRecurring.setCategory(existingCategory);
        transactionRecurring.setUser(existingUser.get());
        transactionRecurring.setWallet(existingWallet);
        transactionRecurring.setAmount(transactionRecurringDTO.getAmount());
        if (existingWallet.getWalletType() == 3) {
            List<SavingGoal> goals = saving_goalsRepository
                    .findByWallet_WalletId(transactionRecurringDTO.getWalletId());
            if (!goals.isEmpty()) {
                Long savingGoalId = transactionRecurringDTO.getSavingGoalId();
                if (savingGoalId == null || savingGoalId == 0) {
                    throw new IllegalArgumentException("A saving goal must be selected for goal-type wallets");
                }
                SavingGoal selectedSavingGoal = goals.stream()
                        .filter(g -> g.getId().equals(savingGoalId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Invalid saving goal ID: " + savingGoalId));

                transactionRecurring.setSavingGoal(selectedSavingGoal);
            }

        }
        TransactionRecurring savedTransaction = transactionRecurringRepository
                .save(transactionRecurring);
        TransactionRecurringDTO savedTransactionDTO = modelMapper.map(savedTransaction, TransactionRecurringDTO.class);
        return ResponseEntity.ok(savedTransactionDTO);
    }

    @Override
    public ResponseEntity<?> updateTransaction(Integer transactionId,
            TransactionRecurringDTO transactionRecurringDTO) {
        Optional<TransactionRecurring> existingTransactionRecurring = (transactionRecurringRepository
                .findById(transactionId));
        if (!existingTransactionRecurring.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (transactionRecurringDTO.getUserId() == 0) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUser = userRepository.findById(transactionRecurringDTO.getUserId());
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + transactionRecurringDTO.getUserId(),
                    HttpStatus.NOT_FOUND);
        }
        Wallet existingWallet = walletService.getWalletById(transactionRecurringDTO.getWalletId());
        if (existingWallet == null) {
            return new ResponseEntity<>(
                    "Wallet not found with id: " + transactionRecurringDTO.getWalletId(),
                    HttpStatus.NOT_FOUND);
        }

        Category existingCategory = categoryService.getByCateId(transactionRecurringDTO.getCategoryId());
        if (existingCategory == null) {
            return new ResponseEntity<>("Category not found with id: " + transactionRecurringDTO.getCategoryId(),
                    HttpStatus.NOT_FOUND);
        }

        if (transactionRecurringDTO.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return new ResponseEntity<>("Amount must be non-negative for transactions", HttpStatus.BAD_REQUEST);
        }

        Recurrence updateRecurrence = recurrenceConverter.convertToEntity(transactionRecurringDTO.getRecurrence());
        updateRecurrence.setStartDate(transactionRecurringDTO.getRecurrence().getStartDate());
        updateRecurrence.setUser(existingUser.get());
        Recurrence savedRecurrence = recurrenceRepository.save(updateRecurrence);
        TransactionRecurring transactionRecurring = existingTransactionRecurring.get();
        transactionRecurring.setRecurrence(savedRecurrence);
        transactionRecurring.setCategory(existingCategory);
        transactionRecurring.setUser(existingUser.get());
        transactionRecurring.setWallet(existingWallet);
        transactionRecurring.setAmount(transactionRecurringDTO.getAmount());
        if (existingWallet.getWalletType() == 3) {
            List<SavingGoal> goals = saving_goalsRepository
                    .findByWallet_WalletId(transactionRecurringDTO.getWalletId());
            if (!goals.isEmpty()) {
                Long savingGoalId = transactionRecurringDTO.getSavingGoalId();
                if (savingGoalId == null || savingGoalId == 0) {
                    throw new IllegalArgumentException("A saving goal must be selected for goal-type wallets");
                }
                SavingGoal selectedSavingGoal = goals.stream()
                        .filter(g -> g.getId().equals(savingGoalId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Invalid saving goal ID: " + savingGoalId));

                transactionRecurring.setSavingGoal(selectedSavingGoal);
            }

        }
        TransactionRecurring savedTransaction = transactionRecurringRepository
                .save(transactionRecurring);
        TransactionRecurringDTO savedTransactionDTO = modelMapper.map(savedTransaction, TransactionRecurringDTO.class);
        return ResponseEntity.ok(savedTransactionDTO);
    }

    @Override
    public TransactionRecurring getTransactionsRecurringById(Integer transactionId) {
        return transactionRecurringRepository.findById(transactionId).orElse(null);
    }

    @Override
    public Page<TransactionRecurring> getAllTransactionsRecurringByUserId(Integer userId, Pageable pageable) {
        return transactionRecurringRepository.findByUserId(userId, pageable);
    }

    @Override
    public ResponseEntity<?> deleteTransaction(Integer transactionId, Authentication authentication) {
        TransactionRecurring transactionRecurring = transactionRecurringRepository
                .findById(transactionId).orElse(null);
        Recurrence recurrence = transactionRecurring.getRecurrence();
        if (recurrence != null) {
            recurrenceRepository.deleteById(recurrence.getRecurrenceId());
        }
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if (!transactionRecurring.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this transaction.");
        }

        if (transactionRecurring == null) {
            return ResponseEntity.notFound().build();
        }
        transactionRecurringRepository.deleteById(transactionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public List<TransactionRecurring> findByRecurrence_DueDate(LocalDate dueDate) {
        return transactionRecurringRepository.findByRecurrence_DueDate(dueDate);
    }

}
