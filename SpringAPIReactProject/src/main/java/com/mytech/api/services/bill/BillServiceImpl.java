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
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceConverter;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;
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

		UserDTO userDTO = billDTO.getUser();
		if (userDTO == null || userDTO.getId() == null) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}
		Optional<User> existingUser = userRepository.findById(userDTO.getId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
		}

		Category existingCategory = categoryService.getByCateId(billDTO.getCategory().getId());
		if (existingCategory == null) {
			return new ResponseEntity<>("Category not found with id: " + billDTO.getCategory().getId(),
					HttpStatus.NOT_FOUND);
		}

		Wallet existingWallet = walletService.getWalletById(billDTO.getWallet().getWalletId());
		if (existingWallet == null) {
			return new ResponseEntity<>("Wallet not found with id: " + billDTO.getWallet().getWalletId(),
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
	public ResponseEntity<?> updateBill(int billId, BillDTO billDTO) {
		Optional<User> userOptional = userRepository.findById(billDTO.getUser().getId());
		if (!userOptional.isPresent()) {
			return new ResponseEntity<>("User not found with id: " +
					billDTO.getUser().getId(), HttpStatus.NOT_FOUND);
		}
		Optional<Bill> existingBillOptional = billRepository.findById(billId);
		if (!existingBillOptional.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Category categoryOptional = categoryService.getByCateId(billDTO.getCategory().getId());
		if (categoryOptional == null) {
			return new ResponseEntity<>("Category not found with id: " +
					billDTO.getCategory().getId(), HttpStatus.NOT_FOUND);
		}
		Wallet walletOptional = walletService.getWalletById(billDTO.getWallet().getWalletId());
		if (walletOptional == null) {
			return new ResponseEntity<>("Wallet not found with id: " +
					billDTO.getWallet().getWalletId(), HttpStatus.NOT_FOUND);
		}
		Recurrence recurrenceOptional = recurrenceRepository.findById(billDTO.getRecurrence().getRecurrenceId())
				.orElse(null);
		if (recurrenceOptional == null) {
			return new ResponseEntity<>("Recurrence not found with id: " +
					billDTO.getRecurrence().getRecurrenceId(), HttpStatus.NOT_FOUND);
		}
		Recurrence updateRecurrence = recurrenceConverter.convertToEntity(billDTO.getRecurrence());
		updateRecurrence.setStartDate(billDTO.getRecurrence().getStartDate());
		updateRecurrence.setUser(userOptional.get());
		recurrenceRepository.save(updateRecurrence);
		Bill existingBill = existingBillOptional.get();
		existingBill.setAmount(billDTO.getAmount());
		existingBill.setRecurrence(updateRecurrence);
		existingBill.setCategory(categoryOptional);
		existingBill.setWallet(walletOptional);
		existingBill.setUser(userOptional.get());

		Bill updatedBill = modelMapper.map(billDTO, Bill.class);
		updatedBill = billRepository.save(updatedBill);
		BillDTO updatedBillDTO = modelMapper.map(updatedBill, BillDTO.class);
		return ResponseEntity.ok(updatedBillDTO);
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

}
