package com.mytech.api.services.saving_goals;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;

import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;

@Service
public  class SavingGoalsServiceImpl implements SavingGoalsService {
    @Autowired
    Saving_goalsRepository savingGoalsRepository;

    @Autowired
    ModelMapper modelMapper;

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
        savingGoal = savingGoalsRepository.save(savingGoal);
        return modelMapper.map(savingGoal, SavingGoalDTO.class);
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
}