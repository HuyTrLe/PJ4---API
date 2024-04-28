package com.mytech.api.controllers.bill;

import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.mytech.api.services.bill.BillService;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.expense.ExpenseService;
import com.mytech.api.services.recurrence.RecurrenceService;
import com.mytech.api.services.wallet.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bills")
public class BillController {

	@Autowired
	BillService billService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RecurrenceService recurrenceService;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	CategoryService categoryService;
	@Autowired
	RecurrenceConverter recurrenceConverter;
	@Autowired
	WalletService walletService;
	@Autowired
	ExpenseService expenseService;

	@PreAuthorize("#billDTO.user.id == authentication.principal.id")
	@PostMapping("/create")
	public ResponseEntity<?> addNewBill(@RequestBody @Valid BillDTO billDTO, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
		}
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
		Recurrence savedRecurrence = recurrenceService.saveRecurrence(newRecurrence);
		bill.setRecurrence(savedRecurrence);
		bill.setCategory(existingCategory);
		bill.setUser(existingUser.get());
		bill.setWallet(existingWallet);
		Bill createdBill = billService.addNewBill(bill);
		BillDTO createdBillDTO = modelMapper.map(createdBill, BillDTO.class);

		return new ResponseEntity<>(createdBillDTO, HttpStatus.CREATED);
	}

	@GetMapping("/{billId}")
	public ResponseEntity<?> getBillById(@PathVariable int billId) {
		Bill bill = billService.findBillById(billId);
		if (bill != null) {
			BillDTO billDTO = modelMapper.map(bill, BillDTO.class);
			return new ResponseEntity<>(billDTO, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<Page<BillDTO>> getAllBillsForUser(@PathVariable int userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		PageRequest pageable = PageRequest.of(page, size);
		Page<BillDTO> billPages = billService.findAllBillByUserId(userId, pageable)
				.map(bill -> {
					BillDTO billDTO = modelMapper.map(bill, BillDTO.class);
					return billDTO;
				});
		return new ResponseEntity<>(billPages, HttpStatus.OK);
	}

	@PreAuthorize("#billDTO.user.id == authentication.principal.id")
	@PutMapping("/update/{billId}")
	public ResponseEntity<?> updateBill(@PathVariable int billId, @RequestBody @Valid BillDTO billDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}
		Optional<User> userOptional = userRepository.findById(billDTO.getUser().getId());
		if (!userOptional.isPresent()) {
			return new ResponseEntity<>("User not found with id: " +
					billDTO.getUser().getId(), HttpStatus.NOT_FOUND);
		}
		Optional<Bill> existingBillOptional = Optional.ofNullable(billService.findBillById(billId));
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
		Recurrence recurrenceOptional = recurrenceService.findRecurrenceById(billDTO.getRecurrence().getRecurrenceId());
		if (recurrenceOptional == null) {
			return new ResponseEntity<>("Recurrence not found with id: " +
					billDTO.getRecurrence().getRecurrenceId(), HttpStatus.NOT_FOUND);
		}
		Recurrence updateRecurrence = recurrenceConverter.convertToEntity(billDTO.getRecurrence());
		updateRecurrence.setStartDate(billDTO.getRecurrence().getStartDate());
		updateRecurrence.setUser(userOptional.get());
		recurrenceService.saveRecurrence(updateRecurrence);
		Bill existingBill = existingBillOptional.get();
		existingBill.setAmount(billDTO.getAmount());
		existingBill.setRecurrence(updateRecurrence);
		existingBill.setCategory(categoryOptional);
		existingBill.setWallet(walletOptional);
		existingBill.setUser(userOptional.get());

		Bill updatedBill = modelMapper.map(billDTO, Bill.class);
		updatedBill = billService.addNewBill(updatedBill);
		BillDTO updatedBillDTO = modelMapper.map(updatedBill, BillDTO.class);
		return ResponseEntity.ok(updatedBillDTO);
	}

	@DeleteMapping("/delete/{billId}")
	public ResponseEntity<?> deleteBill(@PathVariable int billId, Authentication authentication) {
		Bill bill = billService.findBillById(billId);
		if (bill != null) {
			Recurrence recurrence = bill.getRecurrence();
			if (recurrence != null) {
				recurrenceService.deleteRecurrenceById(recurrence.getRecurrenceId());
			}
			billService.deleteBill(billId);
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