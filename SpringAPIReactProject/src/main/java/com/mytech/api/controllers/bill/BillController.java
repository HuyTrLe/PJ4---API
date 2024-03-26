package com.mytech.api.controllers.bill;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.services.bill.BillService;
import com.mytech.api.services.recurrence.RecurrenceService;

@RestController
@RequestMapping("/api/bills")
public class BillController {

	private final BillService billService;
	private final UserRepository userRepository;
	private final RecurrenceService recurrenceService;
	private final ModelMapper modelMapper;

	public BillController(BillService billService, UserRepository userRepository, RecurrenceService recurrenceService,
			ModelMapper modelMapper) {
		this.billService = billService;
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.recurrenceService = recurrenceService;
	}

	@PostMapping
	public ResponseEntity<?> addNewBill(@RequestBody BillDTO billDTO) {
		UserDTO userDTO = billDTO.getUser();
		if (userDTO == null || userDTO.getId() == null) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}

		Optional<User> existingUser = userRepository.findById(userDTO.getId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
		}

		Bill bill = modelMapper.map(billDTO, Bill.class);
		bill.setUser(existingUser.get());

		if (billDTO.getRecurrence() != null) {
			// Already have recurrence
			if (billDTO.getRecurrence().getRecurrenceId() > 0) {
				Recurrence existingRecurrence = recurrenceService
						.findRecurrenceById(billDTO.getRecurrence().getRecurrenceId());
				if (existingRecurrence == null) {
					return new ResponseEntity<>(
							"Recurrence not found with id: " + billDTO.getRecurrence().getRecurrenceId(),
							HttpStatus.NOT_FOUND);
				}
				bill.setRecurrence(existingRecurrence);
			} else {
				// create new recurrence also
				Recurrence newRecurrence = modelMapper.map(billDTO.getRecurrence(), Recurrence.class);
				newRecurrence.setUser(existingUser.get());
				Recurrence savedRecurrence = recurrenceService.saveRecurrence(newRecurrence);
				bill.setRecurrence(savedRecurrence);
			}
		}

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

	@GetMapping("/users/{userId}/bills")
	public ResponseEntity<Map<String, Page<BillDTO>>> getAllBillsForUser(@PathVariable int userId,
			@RequestParam(defaultValue = "0") int overduePage, @RequestParam(defaultValue = "10") int overdueSize,
			@RequestParam(defaultValue = "0") int dueIn3DaysPage, @RequestParam(defaultValue = "10") int dueIn3DaysSize,
			@RequestParam(defaultValue = "0") int futureDuePage, @RequestParam(defaultValue = "10") int futureDueSize) {
		PageRequest overduePageable = PageRequest.of(overduePage, overdueSize);
		PageRequest dueIn3DaysPageable = PageRequest.of(dueIn3DaysPage, dueIn3DaysSize);
		PageRequest futureDuePageable = PageRequest.of(futureDuePage, futureDueSize);

		LocalDate currentDate = LocalDate.now();
		LocalDate dueDate = currentDate.plusDays(3);

		LocalDate overdueDueDate = currentDate;
		LocalDate futureDueDueDate = currentDate.plusDays(4);

		Page<Bill> overdueBillsPage = billService.findOverdueBillsByUserId(userId, overdueDueDate, overduePageable);
		Page<Bill> dueIn3DaysBillsPage = billService.findBillsDueIn3DaysByUserId(userId, dueDate, dueIn3DaysPageable);
		Page<Bill> futureDueBillsPage = billService.findFutureDueBillsByUserId(userId, futureDueDueDate,
				futureDuePageable);

		Map<String, Page<BillDTO>> billPages = new HashMap<>();
		billPages.put("overdueBills", overdueBillsPage.map(bill -> modelMapper.map(bill, BillDTO.class)));
		billPages.put("dueIn3DaysBills", dueIn3DaysBillsPage.map(bill -> modelMapper.map(bill, BillDTO.class)));
		billPages.put("futureDueBills", futureDueBillsPage.map(bill -> modelMapper.map(bill, BillDTO.class)));

		return new ResponseEntity<>(billPages, HttpStatus.OK);
	}

	@PutMapping("/{billId}")
	public ResponseEntity<?> updateBill(@PathVariable int billId, @RequestBody BillDTO billDTO) {
		Optional<User> userOptional = userRepository.findById(billDTO.getUser().getId());
		if (!userOptional.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + billDTO.getUser().getId(), HttpStatus.NOT_FOUND);
		}

		Optional<Bill> existingBillOptional = Optional.ofNullable(billService.findBillById(billId));
		if (!existingBillOptional.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Bill existingBill = existingBillOptional.get();
		existingBill.setBillName(billDTO.getBillName());
		existingBill.setAmount(billDTO.getAmount());
		existingBill.setDueDate(billDTO.getDueDate());

		// Recurrence update, if needed
		// if (billDTO.getRecurrence() != null) {
		// existingBill.setRecurrence(modelMapper.map(billDTO.getRecurrence(),
		// Recurrence.class));
		// }

		existingBill.setUser(userOptional.get());

		Bill updatedBill = billService.addNewBill(existingBill);
		BillDTO updatedBillDTO = modelMapper.map(updatedBill, BillDTO.class);

		return new ResponseEntity<>(updatedBillDTO, HttpStatus.OK);
	}

	@DeleteMapping("/{billId}")
	public ResponseEntity<Void> deleteBill(@PathVariable int billId) {
		Bill bill = billService.findBillById(billId);
		if (bill != null) {
			billService.deleteBill(billId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
}