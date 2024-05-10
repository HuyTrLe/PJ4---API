package com.mytech.api.services.debt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.debt.DebtDTO;
import com.mytech.api.models.debt.ReportDebt;
import com.mytech.api.models.user.User;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.debt.DebtsRepository;

@Service
public class DebtServiceImpl implements DebtService {
    @Autowired
    DebtsRepository debtRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

//    @Override
//    public List<DebtDTO> getAllDebts() {
//        List<Debt> debts = debtRepository.findAll();
//        return debts.stream().map(debt -> modelMapper.map(debt, DebtDTO.class)).collect(Collectors.toList());
//    }

    @Override
    public Page<Debt> getDebtsByUserId(Long userId, Pageable pageable) {
        return debtRepository.findByUserId(userId, pageable);
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
        Debt existingDebt = debtRepository.findById(debtId)
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + debtId));

        existingDebt.setName(updateDebtDTO.getName());
        existingDebt.setAmount(updateDebtDTO.getAmount());
        existingDebt.setDueDate(updateDebtDTO.getDueDate());
        existingDebt.setPaidDate(updateDebtDTO.getPaidDate());
        existingDebt.setIsPaid(updateDebtDTO.getIsPaid());
        existingDebt.setCreditor(updateDebtDTO.getCreditor());
        existingDebt.setNotes(updateDebtDTO.getNotes());

        Category category = categoryRepository.findById(updateDebtDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + updateDebtDTO.getCategoryId()));
        existingDebt.setCategory(category);

        User user = userRepository.findById(updateDebtDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + updateDebtDTO.getUserId()));
        existingDebt.setUser(user);

        Debt updatedDebt = debtRepository.save(existingDebt);

        return modelMapper.map(updatedDebt, DebtDTO.class);
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

	@Override
	public List<Debt> findDebtActive(Long userId) {		
		return debtRepository.findDebtActive(userId);
	}

	@Override
	public List<Debt> findDebtPaid(Long userId) {
		return debtRepository.findDebtPaid(userId);
	}

	@Override
	public List<Debt> findDebt(Long userId) {
		return debtRepository.findDebt(userId);
	}

	@Override
	public List<Debt> findLoan(Long userId) {
		return debtRepository.findLoan(userId);
	}

	@Override
	public List<ReportDebt> ReportDEBT(Long userId) {
		List<ReportDebt> result = new ArrayList<>();
		LocalDate currentDate = LocalDate.now();
		var debtTTH = debtRepository.GetDebtTTH(userId);
		result.add(debtTTH);
		var GetDebtTSTH = debtRepository.GetDebtTSTH(userId);
		result.add(GetDebtTSTH);
		var GetDebtCTCTTH = debtRepository.GetDebtCTCTTH(userId,currentDate);
		result.add(GetDebtCTCTTH);
		var GetDebtCTQTH = debtRepository.GetDebtCTQTH(userId,currentDate);
		result.add(GetDebtCTQTH);
		
		var GetLoanTTH = debtRepository.GetLoanTTH(userId);
		result.add(GetLoanTTH);
		var GetLoanTSTH = debtRepository.GetLoanTSTH(userId);
		result.add(GetLoanTSTH);
		var GetLoanCTCTTH = debtRepository.GetLoanCTCTTH(userId,currentDate);
		result.add(GetLoanCTCTTH);
		var GetLoanCTQTH = debtRepository.GetLoanCTQTH(userId,currentDate);
		result.add(GetLoanCTQTH);
		
		
		return result;
	}
	
	


}