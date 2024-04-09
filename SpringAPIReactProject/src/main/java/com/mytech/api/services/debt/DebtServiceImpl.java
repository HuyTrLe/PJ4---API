package com.mytech.api.services.debt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.debt.DebtDTO;

import com.mytech.api.repositories.debt.DebtsRepository;

@Service
public class DebtServiceImpl implements DebtService {
    @Autowired
    DebtsRepository debtRepository;

    @Autowired
    ModelMapper modelMapper;

//    @Override
//    public List<DebtDTO> getAllDebts() {
//        List<Debt> debts = debtRepository.findAll();
//        return debts.stream().map(debt -> modelMapper.map(debt, DebtDTO.class)).collect(Collectors.toList());
//    }

    @Override
    public List<DebtDTO> getDebtsByUserId(Long userId) {
        List<Debt> debts = debtRepository.findByUserId(userId);
        return debts.stream().map(debt -> modelMapper.map(debt, DebtDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDebtById(Long debtId) {
        debtRepository.deleteById(debtId);
    }

    @Override
    public boolean existsDebtById(Long debtId) {
        return debtRepository.existsById(debtId);
    }

    @Override
    public DebtDTO createDebt(DebtDTO debtDTO) {
        Debt debt = modelMapper.map(debtDTO, Debt.class);
        debt = debtRepository.save(debt);
        return modelMapper.map(debt, DebtDTO.class);
    }

    @Override
    @Transactional
    public DebtDTO updateDebt(Long debtId, DebtDTO updateDebtDTO) {
        Optional<Debt> existingDebtOptional = debtRepository.findById(debtId);
        if (!existingDebtOptional.isPresent()) {
            throw new IllegalArgumentException("Debt not found with ID: " + debtId);
        }
        Debt existingDebt = existingDebtOptional.get();
        modelMapper.map(updateDebtDTO, existingDebt);
        existingDebt = debtRepository.save(existingDebt);
        return modelMapper.map(existingDebt, DebtDTO.class);
    }

    @Override
    public DebtDTO getDebtById(Long debtId) {
        Optional<Debt> debtOptional = debtRepository.findById(debtId);
        if (debtOptional.isPresent()) {
            Debt debt = debtOptional.get();
            return modelMapper.map(debt, DebtDTO.class);
        } else {
            throw new IllegalArgumentException("Debt not found with ID: " + debtId);
        }
    }
}