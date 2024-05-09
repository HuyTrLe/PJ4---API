package com.mytech.api.services.saving_goals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.models.notifications.NotificationType;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
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

        // Update wallet balance if current amount > 0
        if (savingGoal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newBalance = wallet.getBalance().add(savingGoal.getCurrentAmount());
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
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

        SavingGoal createdSavingGoal = savingGoalsRepository.save(savingGoal);

        // Send notifications if needed
        // if
        // (createdSavingGoal.getCurrentAmount().compareTo(createdSavingGoal.getTargetAmount())
        // >= 0) {
        // sendSavingGoalLimitNotification(createdSavingGoal);
        // }

        // sendSavingGoalNotification(createdSavingGoal);

        return modelMapper.map(createdSavingGoal, SavingGoalDTO.class);
    }

    private void sendSavingGoalLimitNotification(SavingGoal savingGoal) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(savingGoal.getUser().getId());
        notificationDTO.setNotificationType(NotificationType.SAVING_GOAL_LIMIT);
        notificationDTO.setEventId(savingGoal.getId());
        notificationDTO.setMessage("Your budget for " + savingGoal.getName() + " has reached its limit.");
        notificationDTO.setTimestamp(LocalDateTime.now());
        notificationService.sendNotification(notificationDTO);
    }

    private void sendSavingGoalNotification(SavingGoal savingGoal) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), savingGoal.getEndDate());
        if (daysUntilDue <= 3) {
            NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setUserId(savingGoal.getUser().getId());
            notificationDTO.setNotificationType(NotificationType.SAVING_GOAL_DUE);
            notificationDTO.setEventId(savingGoal.getId());
            notificationDTO.setMessage("Your goal for " + savingGoal.getName() + " is due in 3 days or less.");
            notificationDTO.setTimestamp(LocalDateTime.now());
            notificationService.sendNotification(notificationDTO);
        }
    }

    private void sendLimitNotification(SavingGoal savingGoal) {
        // Create and send a notification for goal limit
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(savingGoal.getUser().getId());
        notificationDTO.setNotificationType(NotificationType.SAVING_GOAL_LIMIT);
        notificationDTO.setEventId(Long.valueOf(savingGoal.getId()));
        notificationDTO.setMessage("Your goal for " + savingGoal.getName() + " has reached its limit.");
        notificationDTO.setTimestamp(LocalDateTime.now());
        notificationService.sendNotification(notificationDTO);
    }

    private void sendDueNotification(SavingGoal savingGoal) {
        // Create and send a notification for goal due
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(savingGoal.getUser().getId());
        notificationDTO.setNotificationType(NotificationType.SAVING_GOAL_DUE);
        notificationDTO.setEventId(Long.valueOf(savingGoal.getId()));
        notificationDTO.setMessage(
                "Your goal for " + savingGoal.getName() + " is about to be due in 3 days or less.");
        notificationDTO.setTimestamp(LocalDateTime.now());
        notificationService.sendNotification(notificationDTO);
    }

    public void checkGoalsPeriodically() {
        List<SavingGoal> allSavingGoals = savingGoalsRepository.findAll();
        LocalDate today = LocalDate.now();

        for (SavingGoal savingGoal : allSavingGoals) {
            // Check if the goal reaches its limit
            if (savingGoal.getCurrentAmount().compareTo(savingGoal.getTargetAmount()) >= 0) {
                sendLimitNotification(savingGoal);
            }

            // Check if the goal is about to be due
            long daysUntilDue = ChronoUnit.DAYS.between(today, savingGoal.getEndDate());
            if (daysUntilDue <= 3) {
                sendDueNotification(savingGoal);
            }
        }
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

}