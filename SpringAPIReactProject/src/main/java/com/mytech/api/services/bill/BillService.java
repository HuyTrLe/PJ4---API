package com.mytech.api.services.bill;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.bill.BillDTO;
import com.mytech.api.models.debt.ReportDebtParam;

public interface BillService {

	List<Bill> findAllBill();

	Page<Bill> findAllBillByUserId(int userId, Pageable pageable);

	Bill findBillById(int billId);

	ResponseEntity<?> addNewBill(BillDTO billDTO);

	BillDTO updateBill(int billId, BillDTO billDTO);

	List<Bill> findByRecurrence_DueDate(LocalDate dueDate);

	ResponseEntity<?> deleteBill(int billId, Authentication authentication);
	
	List<Bill> findBillActive(ReportDebtParam param);

	List<Bill> findBillExpired(ReportDebtParam param);

}
