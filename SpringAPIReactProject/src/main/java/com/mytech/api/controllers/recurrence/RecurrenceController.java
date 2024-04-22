package com.mytech.api.controllers.recurrence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.recurrence.RecurrenceType;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.services.recurrence.RecurrenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recurrences")
public class RecurrenceController {

    private final RecurrenceService recurrenceService;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    public RecurrenceController(RecurrenceService recurrenceService, UserRepository userRepository,
            ModelMapper modelMapper) {
        this.recurrenceService = recurrenceService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    // Schedule a new recurring transaction/bill
    @PostMapping("/create")
    public ResponseEntity<?> createRecurrence(@RequestBody @Valid RecurrenceDTO recurrenceDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            System.out.println(errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
        }
        validateRecurrenceType(recurrenceDTO.getRecurrenceType());

        // Find the user based on the ID set in the Recurrence's User object
        UserDTO userDTO = recurrenceDTO.getUser();
        if (userDTO == null || userDTO.getId() == null) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        User user = modelMapper.map(userDTO, User.class);

        Optional<User> existingUser = userRepository.findById(user.getId());
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + user.getId(), HttpStatus.NOT_FOUND);
        }
        Recurrence recurrence = modelMapper.map(recurrenceDTO, Recurrence.class);

        // Associate the existing user with the recurrence
        recurrence.setUser(existingUser.get());

        // save new
        Recurrence newRecurrence = recurrenceService.saveRecurrence(recurrence);
        RecurrenceDTO newRecurrenceDTO = modelMapper.map(newRecurrence, RecurrenceDTO.class);
        return new ResponseEntity<>(newRecurrenceDTO, HttpStatus.CREATED);
    }

    // Get details of a recurrence
    @GetMapping("/{recurrenceId}")
    public ResponseEntity<RecurrenceDTO> getRecurrenceById(@PathVariable int recurrenceId) {
        Recurrence recurrence = recurrenceService.findRecurrenceById(recurrenceId);
        if (recurrence != null) {
            RecurrenceDTO recurrenceDTO = modelMapper.map(recurrence, RecurrenceDTO.class);
            return new ResponseEntity<>(recurrenceDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update recurrence details
    @PutMapping("/update/{recurrenceId}")
    public ResponseEntity<?> updateRecurrence(@PathVariable int recurrenceId,
            @RequestBody RecurrenceDTO recurrenceDTO) {
        if (recurrenceDTO.getUser() == null || recurrenceDTO.getUser().getId() == null) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findById(recurrenceDTO.getUser().getId());
        if (!user.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + recurrenceDTO.getUser().getId(),
                    HttpStatus.NOT_FOUND);
        }

        Optional<Recurrence> existingRecurrenceOpt = Optional
                .ofNullable(recurrenceService.findRecurrenceById(recurrenceId));
        if (!existingRecurrenceOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Recurrence existingRecurrence = existingRecurrenceOpt.get();
        // Map properties from RecurrenceDTO to existingRecurrence
        existingRecurrence.setUser(modelMapper.map(recurrenceDTO.getUser(), User.class));
        existingRecurrence.setRecurrenceType(recurrenceDTO.getRecurrenceType());
        existingRecurrence.setStartDate(recurrenceDTO.getStartDate());
        existingRecurrence.setEndDate(recurrenceDTO.getEndDate());
        existingRecurrence.setIntervalAmount(recurrenceDTO.getIntervalAmount());

        validateRecurrenceType(existingRecurrence.getRecurrenceType());

        Recurrence updatedRecurrence = recurrenceService.saveRecurrence(existingRecurrence);
        return new ResponseEntity<>(updatedRecurrence, HttpStatus.OK);
    }

    // Get all recurrences for a specific user
    @GetMapping("/userRecurrence/{userId}")
    public ResponseEntity<List<RecurrenceDTO>> getAllRecurrencesByUser(@PathVariable Long userId) {
        List<Recurrence> recurrences = recurrenceService.findRecurrencesByUserId(userId);

        if (recurrences.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<RecurrenceDTO> recurrenceDTOs = recurrences.stream()
                .map(recurrence -> modelMapper.map(recurrence, RecurrenceDTO.class))
                .collect(Collectors.toList());

        return new ResponseEntity<>(recurrenceDTOs, HttpStatus.OK);
    }

    // Delete a recurrence
    @DeleteMapping("/delete/{recurrenceId}")
    public ResponseEntity<Void> deleteRecurrence(@PathVariable int recurrenceId) {
        // Check if recurrence exists
        Recurrence existingRecurrence = recurrenceService.findRecurrenceById(recurrenceId);
        if (existingRecurrence == null) {
            // 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            recurrenceService.deleteRecurrenceById(recurrenceId);
            // Return 204 : successful deletion
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private void validateRecurrenceType(RecurrenceType recurrenceType) {
        if (recurrenceType == null) {
            throw new IllegalArgumentException("Invalid or missing recurrence type.");
        }
    }
}