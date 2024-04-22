package com.mytech.api.controllers.bill;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.services.bill.BillService;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.recurrence.RecurrenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bills")
public class BillController {

	private final BillService billService;
	private final UserRepository userRepository;
	private final RecurrenceService recurrenceService;
	private final ModelMapper modelMapper;
	private final CategoryService categoryService;

	public BillController(BillService billService, UserRepository userRepository, RecurrenceService recurrenceService,
			ModelMapper modelMapper, CategoryService categoryService) {
		this.billService = billService;
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.recurrenceService = recurrenceService;
		this.categoryService = categoryService;
	}

	@PostMapping("/create")
	public ResponseEntity<?> addNewBill(@RequestBody @Valid BillDTO billDTO, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
		}
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

		Category existingCategory = categoryService.getByCateId(billDTO.getCategory().getId());
		if (existingCategory == null) {
			return new ResponseEntity<>("Category not found with id: " + billDTO.getCategory().getId(),
					HttpStatus.NOT_FOUND);
		}

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
		bill.setCategory(existingCategory);

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
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		PageRequest pageable = PageRequest.of(page, size);

		LocalDate currentDate = LocalDate.now();
		LocalDate dueIn3DaysDate = currentDate.plusDays(3);
		LocalDate futureDueDate = currentDate.plusDays(4);

		Page<BillDTO> overdueBillsPage = billService.findOverdueBillsByUserId(userId, currentDate, pageable)
				.map(bill -> modelMapper.map(bill, BillDTO.class));
		Page<BillDTO> dueIn3DaysBillsPage = billService
				.findBillsDueIn3DaysByUserId(userId, currentDate, dueIn3DaysDate, pageable)
				.map(bill -> modelMapper.map(bill, BillDTO.class));
		Page<BillDTO> futureDueBillsPage = billService.findFutureDueBillsByUserId(userId, futureDueDate, pageable)
				.map(bill -> modelMapper.map(bill, BillDTO.class));

		Map<String, Page<BillDTO>> billPages = new HashMap<>();
		billPages.put("overdueBills", overdueBillsPage);
		billPages.put("dueIn3DaysBills", dueIn3DaysBillsPage);
		billPages.put("futureDueBills", futureDueBillsPage);

		return new ResponseEntity<>(billPages, HttpStatus.OK);
	}

	@PutMapping("/update/{billId}")
	public ResponseEntity<?> updateBill(@PathVariable int billId, @RequestBody @Valid BillDTO billDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}

		Bill updatedBill = modelMapper.map(billDTO, Bill.class);
		updatedBill.setBillId(billId);

		// Optional<User> userOptional =
		// userRepository.findById(billDTO.getUser().getId());
		// if (!userOptional.isPresent()) {
		// return new ResponseEntity<>("User not found with id: " +
		// billDTO.getUser().getId(), HttpStatus.NOT_FOUND);
		// }

		// Optional<Bill> existingBillOptional =
		// Optional.ofNullable(billService.findBillById(billId));
		// if (!existingBillOptional.isPresent()) {
		// return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		// }

		// Bill existingBill = existingBillOptional.get();
		// existingBill.setBillName(billDTO.getBillName());
		// existingBill.setAmount(billDTO.getAmount());
		// existingBill.setDueDate(billDTO.getDueDate());
		// // Recurrence update, if needed
		// // if (billDTO.getRecurrence() != null) {
		// // existingBill.setRecurrence(modelMapper.map(billDTO.getRecurrence(),
		// // Recurrence.class));
		// // }

		// existingBill.setUser(userOptional.get());

		// Bill updatedBill = billService.addNewBill(existingBill);
		// BillDTO updatedBillDTO = modelMapper.map(updatedBill, BillDTO.class);
		updatedBill = billService.addNewBill(updatedBill);
		BillDTO updatedBillDTO = modelMapper.map(updatedBill, BillDTO.class);

		return ResponseEntity.ok(updatedBillDTO);
	}

	@DeleteMapping("/delete/{billId}")
	public ResponseEntity<Void> deleteBill(@PathVariable int billId) {
		Bill bill = billService.findBillById(billId);
		if (bill != null) {
			billService.deleteBill(billId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

	// @Scheduled(cron = "00 37 18 * * *")
	// @Transactional
	// public void createNewBillsForDueDate() {
	// LocalDate currentDate = LocalDate.now();
	// List<BillDTO> billsDueToday =
	// billService.findBillsDueToday(currentDate).stream().map(this::mapBillToDTO)
	// .collect(Collectors.toList());
	//
	// for (BillDTO billDTO : billsDueToday) {
	// RecurrenceDTO originalRecurrence = billDTO.getRecurrence();
	// if (originalRecurrence != null) {
	// LocalDate endDate = originalRecurrence.getEndDate();
	// if (endDate != null && currentDate.isAfter(endDate)) {
	// break;
	// }
	//
	// Recurrence originalRecurrenceEntity = recurrenceService
	// .findRecurrenceById(originalRecurrence.getRecurrenceId());
	// if (originalRecurrenceEntity != null) {
	// LocalDate nextDueDate = calculateNextDueDate(originalRecurrence,
	// currentDate);
	// Bill newBill = new Bill();
	// newBill.setUser(modelMapper.map(billDTO.getUser(), User.class));
	// newBill.setBillName(billDTO.getBillName());
	// newBill.setAmount(billDTO.getAmount());
	// newBill.setDueDate(nextDueDate);
	// newBill.setRecurrence(originalRecurrenceEntity);
	// billService.addNewBill(newBill);
	// }
	// }
	// }
	// }
	//
	// private LocalDate calculateNextDueDate(RecurrenceDTO recurrence, LocalDate
	// currentDueDate) {
	// switch (recurrence.getRecurrenceType()) {
	// case DAILY:
	// return currentDueDate.plusDays(recurrence.getIntervalAmount());
	// case WEEKLY:
	// return currentDueDate.plusWeeks(recurrence.getIntervalAmount());
	// case MONTHLY:
	// return currentDueDate.plusMonths(recurrence.getIntervalAmount());
	// case ANNUALLY:
	// return currentDueDate.plusYears(recurrence.getIntervalAmount());
	// default:
	// throw new IllegalArgumentException("Unsupported recurrence type: " +
	// recurrence.getRecurrenceType());
	// }
	// }
	//
	// private BillDTO mapBillToDTO(Bill bill) {
	// BillDTO billDTO = modelMapper.map(bill, BillDTO.class);
	// return billDTO;
	// }

}