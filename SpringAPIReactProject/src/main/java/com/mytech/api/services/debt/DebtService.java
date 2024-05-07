package com.mytech.api.services.debt;

import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.debt.DebtDTO;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface DebtService {

    //List<DebtDTO> getAllDebts();

    List<DebtDTO> getDebtsByUserId(Long userId);

    DebtDTO getDebtById(Long debtId);

    DebtDTO createDebt(DebtDTO debtRequest);

    DebtDTO updateDebt(Long debtId, DebtDTO updatedDebtDTO);

    void deleteDebtById(Long debtId);

    boolean existsDebtById(Long debtId);
    

    List<Debt> findDebtActive(Long userId);

    List<Debt> findDebtPaid(Long userId);
    
}
