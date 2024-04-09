package com.mytech.api.services.bill;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.repositories.bill.BillRepository;
import com.mytech.api.services.recurrence.RecurrenceService;

@Service
public class BillServiceImpl implements BillService {

	private final BillRepository billRepository;
	private final RecurrenceService recurrenceService;

	public BillServiceImpl(BillRepository billRepository, RecurrenceService recurrenceService) {
		this.billRepository = billRepository;
		this.recurrenceService = recurrenceService;
	}

	@Override
	public List<Bill> findAllBill() {
		return billRepository.findAll();
	}

	@Override
	public Page<Bill> findAllBillByUserId(int userId, Pageable pageable) {
		return billRepository.findAllBillByUserId(userId, pageable);
	}

	@Override
	public Bill findBillById(int billId) {
		return billRepository.findById(billId).orElse(null);
	}

	@Override
	public Bill addNewBill(Bill bill) {
		if (bill.getRecurrence() != null && bill.getRecurrence().getRecurrenceId() > 0) {
			Recurrence recurrence = recurrenceService.findRecurrenceById(bill.getRecurrence().getRecurrenceId());
			if (recurrence == null) {
				throw new IllegalArgumentException(
						"Recurrence with ID " + bill.getRecurrence().getRecurrenceId() + " does not exist.");
			}
			bill.setRecurrence(recurrence);
		}
		return billRepository.save(bill);
	}

	@Override
	public void deleteBill(int billId) {
		billRepository.deleteById(billId);
	}
	
	@Override
	public void deleteBillsByRecurrence(int recurrenceId) {
        List<Bill> bills = billRepository.findByRecurrence_RecurrenceId(recurrenceId);
        billRepository.deleteAll(bills);
    }

	@Override
	public Page<Bill> findOverdueBillsByUserId(int userId, LocalDate overdueDueDate, Pageable pageable) {
		return billRepository.findOverdueBillsByUserId(userId, overdueDueDate, pageable);
	}

	@Override
	public Page<Bill> findBillsDueIn3DaysByUserId(int userId, LocalDate currentDate, LocalDate dueDate,
			Pageable pageable) {
		return billRepository.findBillsDueIn3DaysByUserId(userId, currentDate, dueDate, pageable);
	}

	@Override
	public Page<Bill> findFutureDueBillsByUserId(int userId, LocalDate futureDueDueDate, Pageable pageable) {
		return billRepository.findFutureDueBillsByUserId(userId, futureDueDueDate, pageable);
	}
	
	@Override
    public List<Bill> findBillsDueToday(LocalDate dueDate) {
        return billRepository.findByDueDate(dueDate);
    }

}
