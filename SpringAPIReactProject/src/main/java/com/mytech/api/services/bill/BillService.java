package com.mytech.api.services.bill;

import java.util.List;

import com.mytech.api.models.bill.Bill;

import jakarta.transaction.Transactional;

public interface BillService {
	
	List<Bill> findAllBill();
	
	List<Bill> findAllBillByUserId(int userId);
	
	Bill findBillById(int billId);
	
	Bill addNewBill(Bill bill);
	
	void deleteBill(int billId);
}
