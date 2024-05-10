package com.mytech.api.services.debt;

import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.debt.DebtDTO;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface DebtService {

    //List<DebtDTO> getAllDebts();

    Page<Debt> getDebtsByUserId(Long userId, Pageable pageable);

    DebtDTO getDebtById(Long debtId);

    DebtDTO createDebt(DebtDTO debtRequest);

    DebtDTO updateDebt(Long debtId, DebtDTO updatedDebtDTO);

    void deleteDebtById(Long debtId);

    boolean existsDebtById(Long debtId);
    

    List<Debt> findDebtActive(Long userId);

    List<Debt> findDebtPaid(Long userId);
    
}
