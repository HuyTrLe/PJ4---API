package com.mytech.api.services.bill;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.bill.BillDTO;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.debt.ReportDebtParam;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceConverter;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.bill.BillRepository;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.wallet.WalletService;

@Service
public class BillServiceImpl implements BillService {

	@Autowired
	BillRepository billRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	CategoryService categoryService;
	@Autowired
	RecurrenceConverter recurrenceConverter;
	@Autowired
	RecurrenceRepository recurrenceRepository;
	@Autowired
	WalletService walletService;

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
	public List<Bill> findByRecurrence_DueDate(LocalDate dueDate) {
		return billRepository.findByRecurrence_DueDate(dueDate);
	}

	@Override
	public ResponseEntity<?> addNewBill(BillDTO billDTO) {
		Bill bill = modelMapper.map(billDTO, Bill.class);

		if (billDTO.getUserId() == 0) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}
		Optional<User> existingUser = userRepository.findById(billDTO.getUserId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + billDTO.getUserId(), HttpStatus.NOT_FOUND);
		}

		Category existingCategory = categoryService.getByCateId(billDTO.getCategoryId());
		if (existingCategory == null) {
			return new ResponseEntity<>("Category not found with id: " + billDTO.getCategoryId(),
					HttpStatus.NOT_FOUND);
		}

		Wallet existingWallet = walletService.getWalletById(billDTO.getWalletId());
		if (existingWallet == null) {
			return new ResponseEntity<>("Wallet not found with id: " + billDTO.getWalletId(),
					HttpStatus.NOT_FOUND);
		}

		Recurrence newRecurrence = recurrenceConverter.convertToEntity(billDTO.getRecurrence());
		newRecurrence.setStartDate(billDTO.getRecurrence().getStartDate());
		newRecurrence.setUser(existingUser.get());
		Recurrence savedRecurrence = recurrenceRepository.save(newRecurrence);
		bill.setRecurrence(savedRecurrence);
		bill.setCategory(existingCategory);
		bill.setUser(existingUser.get());
		bill.setWallet(existingWallet);
		Bill createdBill = billRepository.save(bill);
		BillDTO createdBillDTO = modelMapper.map(createdBill, BillDTO.class);

		return new ResponseEntity<>(createdBillDTO, HttpStatus.CREATED);
	}

	@Override
	public BillDTO updateBill(int billId, BillDTO billDTO) {
		Optional<User> userOptional = userRepository.findById(billDTO.getUserId());
		Bill existingBill = billRepository.findById(billId)
				.orElseThrow(() -> new RuntimeException("Bill not found with id: " + billId));
		Category categoryOptional = categoryService.getByCateId(billDTO.getCategoryId());
		Wallet walletOptional = walletService.getWalletById(billDTO.getWalletId());
		Recurrence updateRecurrence = recurrenceConverter.convertToEntity(billDTO.getRecurrence());
		updateRecurrence.setStartDate(billDTO.getRecurrence().getStartDate());
		updateRecurrence.setUser(userOptional.get());
		recurrenceRepository.save(updateRecurrence);
		existingBill.setAmount(billDTO.getAmount());
		existingBill.setRecurrence(updateRecurrence);
		existingBill.setCategory(categoryOptional);
		existingBill.setWallet(walletOptional);
		existingBill.setUser(userOptional.get());

		Bill updatedBill = modelMapper.map(billDTO, Bill.class);
		updatedBill = billRepository.save(updatedBill);
		return modelMapper.map(updatedBill, BillDTO.class);
	}

	@Override
	public ResponseEntity<?> deleteBill(int billId, Authentication authentication) {
		Bill bill = billRepository.findById(billId).orElse(null);
		if (bill != null) {
			Recurrence recurrence = bill.getRecurrence();
			if (recurrence != null) {
				recurrenceRepository.deleteById(recurrence.getRecurrenceId());
			}
			billRepository.deleteById(billId);
			MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
			if (!bill.getUser().getId().equals(userDetails.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You are not authorized to delete this transaction.");
			}
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

	@Override
	public List<Bill> findBillActive(ReportDebtParam param) {
		LocalDate currentDate = LocalDate.now();
		return billRepository.findBillActive(param.getUserId(), currentDate,param.getFromDate(),param.getToDate());
	}

	@Override
	public List<Bill> findBillExpired(ReportDebtParam param) {
		LocalDate currentDate = LocalDate.now();
		return billRepository.findBillExpired(param.getUserId(), currentDate,param.getFromDate(),param.getToDate());
	}

}
