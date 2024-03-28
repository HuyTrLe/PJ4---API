package com.mytech.api.services.bill;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mytech.api.models.bill.Bill;

public interface BillService {

	List<Bill> findAllBill();

	Page<Bill> findAllBillByUserId(int userId, Pageable pageable);

	Bill findBillById(int billId);

	Bill addNewBill(Bill bill);

	void deleteBill(int billId);

	Page<Bill> findOverdueBillsByUserId(int userId, LocalDate overdueDueDate, Pageable pageable);

	Page<Bill> findBillsDueIn3DaysByUserId(int userId, LocalDate currentDate, LocalDate dueDate, Pageable pageable);

	Page<Bill> findFutureDueBillsByUserId(int userId, LocalDate futureDueDueDate, Pageable pageable);
}