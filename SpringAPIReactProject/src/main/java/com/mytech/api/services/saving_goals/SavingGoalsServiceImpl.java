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
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.services.notification.NotificationService;

@Service
public class SavingGoalsServiceImpl implements SavingGoalsService {
    @Autowired
    Saving_goalsRepository savingGoalsRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    NotificationService notificationService;

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
        savingGoalsRepository.deleteById(savingGoalId);
    }

    @Override
    public boolean existsSavingGoalById(Long savingGoalId) {
        return savingGoalsRepository.existsById(savingGoalId);
    }

    @Override
    public SavingGoalDTO createSavingGoal(SavingGoalDTO savingGoalDTO) {
        SavingGoal savingGoal = modelMapper.map(savingGoalDTO, SavingGoal.class);

        if (savingGoalDTO.getEndDateType() == null) {
            throw new IllegalArgumentException("You need to choose either Forever or End Date for Goals");
        } else if ("FOREVER".equals(savingGoalDTO.getEndDateType())) {
            savingGoal.setEndDate(null); // End Date null if choose FOREVER
        } else if ("END_DATE".equals(savingGoalDTO.getEndDateType())) {
            if (savingGoalDTO.getEndDate() == null) {
                throw new IllegalArgumentException("Please select an End Date for the goal");
            }
            savingGoal.setEndDate(savingGoalDTO.getEndDate());
        }

        SavingGoal createdSavingGoal = savingGoalsRepository.save(savingGoal);
        SavingGoalDTO newSavingGoalDTO = convertToDTO(createdSavingGoal);
        return modelMapper.map(newSavingGoalDTO, SavingGoalDTO.class);
    }

    @Override
    public void adjustSavingGoalForTransaction(Transaction transaction, boolean isDeletion, BigDecimal oldAmount) {
        Long categoryId = transaction.getCategory().getId();
        Long userId = transaction.getUser().getId();
        SavingGoal savingGoal = savingGoalsRepository.findByUserIdAndCategory_Id(userId, categoryId);

        if (savingGoal != null) {
            BigDecimal amountChange;

            if (isDeletion) {
                amountChange = transaction.getAmount().negate();
            } else {
                amountChange = transaction.getAmount().subtract(oldAmount);
            }
            savingGoal.setCurrentAmount(savingGoal.getCurrentAmount().add(amountChange));

            savingGoalsRepository.save(savingGoal);
        }
    }

    private void sendLimitNotification(SavingGoal savingGoal) {
        // Create and send a notification for goal limit
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(savingGoal.getUser().getId());
        notificationDTO.setNotificationType(NotificationType.SAVING_GOAL_LIMIT);
        notificationDTO.setEventId(Long.valueOf(savingGoal.getId()));
        notificationDTO.setMessage("Your goal for " + savingGoal.getCategory().getName() + " has reached its limit.");
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
                "Your goal for " + savingGoal.getCategory().getName() + " is about to be due in 3 days or less.");
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
        Optional<SavingGoal> existingSavingGoalOptional = savingGoalsRepository.findById(savingGoalId);
        if (!existingSavingGoalOptional.isPresent()) {
            throw new IllegalArgumentException("Saving Goal not found with ID: " + savingGoalId);
        }
        SavingGoal existingSavingGoal = existingSavingGoalOptional.get();
        modelMapper.map(updateSavingGoalDTO, existingSavingGoal);
        existingSavingGoal = savingGoalsRepository.save(existingSavingGoal);
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

    private SavingGoalDTO convertToDTO(SavingGoal savingGoal) {
        return modelMapper.map(savingGoal, SavingGoalDTO.class);
    }
}