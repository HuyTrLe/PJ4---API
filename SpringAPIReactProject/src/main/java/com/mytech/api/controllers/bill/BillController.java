package com.mytech.api.controllers.bill;

import java.util.List;
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
import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.bill.BillDTO;
import com.mytech.api.models.bill.BillResponse;
import com.mytech.api.models.budget.BudgetDTO;
import com.mytech.api.models.recurrence.RecurrenceConverter;
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

	@PreAuthorize("#billDTO.userId == authentication.principal.id")
	@PostMapping("/create")
	public ResponseEntity<?> addNewBill(@RequestBody @Valid BillDTO billDTO, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}
		return billService.addNewBill(billDTO);
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
	
	@GetMapping("findBillActive/users/{userId}")
	public ResponseEntity<?> findBillActive(@PathVariable int userId) {
		List<Bill> bill = billService.findBillActive(userId);
		if (bill != null) {
			List<BillResponse> billDTOs = bill.stream()
	                .map(bills -> modelMapper.map(bills, BillResponse.class))
	                .collect(Collectors.toList());
			 return ResponseEntity.ok(billDTOs);
		}	
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("findBillExpired/users/{userId}")
	public ResponseEntity<?> findBillExpired(@PathVariable int userId) {
		List<Bill> bill = billService.findBillExpired(userId);
		if (bill != null) {
			List<BillResponse> billDTOs = bill.stream()
	                .map(bills -> modelMapper.map(bills, BillResponse.class))
	                .collect(Collectors.toList());
			 return ResponseEntity.ok(billDTOs);
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

	@PreAuthorize("#billDTO.userId == authentication.principal.id")
	@PutMapping("/update/{billId}")
	public ResponseEntity<?> updateBill(@PathVariable int billId, @RequestBody @Valid BillDTO billDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}
		BillDTO updateBillDTO = billService.updateBill(billId, billDTO);
		return ResponseEntity.ok(updateBillDTO);
	}

	@DeleteMapping("/delete/{billId}")
	public ResponseEntity<?> deleteBill(@PathVariable int billId, Authentication authentication) {
		return billService.deleteBill(billId, authentication);
	}

}