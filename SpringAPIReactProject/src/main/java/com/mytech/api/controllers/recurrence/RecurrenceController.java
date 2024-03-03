package com.mytech.api.controllers.recurrence;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
import com.mytech.api.models.recurrence.RecurrenceType;
import com.mytech.api.models.user.User;
import com.mytech.api.services.recurrence.RecurrenceService;

@RestController
@RequestMapping("/api/recurrences")
public class RecurrenceController {

    private final RecurrenceService recurrenceService;
    
    private final UserRepository userRepository;

    public RecurrenceController(RecurrenceService recurrenceService, UserRepository userRepository) {
        this.recurrenceService = recurrenceService;
        this.userRepository = userRepository; 
    }

    // Schedule a new recurring transaction/bill
    @PostMapping
    public ResponseEntity<?> createRecurrence(@RequestBody Recurrence recurrence) {
        validateRecurrenceType(recurrence.getRecurrenceType());

        // Find the user based on the ID set in the Recurrence's User object
        User user = recurrence.getUser();
        if (user == null || user.getId() == null) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUser = userRepository.findById(user.getId());
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + user.getId(), HttpStatus.NOT_FOUND);
        }

        // Associate the existing user with the recurrence
        recurrence.setUser(existingUser.get());

        //save new
        Recurrence newRecurrence = recurrenceService.saveRecurrence(recurrence);                                                                                
        return new ResponseEntity<>(newRecurrence, HttpStatus.CREATED);
    }

    // Get details of a recurrence
    @GetMapping("/{recurrenceId}")
    public ResponseEntity<Recurrence> getRecurrenceById(@PathVariable int recurrenceId) {
        Recurrence recurrence = recurrenceService.findRecurrenceById(recurrenceId);
        return recurrence != null ? new ResponseEntity<>(recurrence, HttpStatus.OK)
                                  : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Update recurrence details
    @PutMapping("/{recurrenceId}")
    public ResponseEntity<?> updateRecurrence(@PathVariable int recurrenceId, @RequestBody Recurrence recurrence) {
        if (recurrence.getUser() == null || recurrence.getUser().getId() == null) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findById(recurrence.getUser().getId());
        if (!user.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + recurrence.getUser().getId(), HttpStatus.NOT_FOUND);
        }

        Optional<Recurrence> existingRecurrenceOpt = Optional.ofNullable(recurrenceService.findRecurrenceById(recurrenceId));
        if (!existingRecurrenceOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Recurrence existingRecurrence = existingRecurrenceOpt.get();
        existingRecurrence.setUser(user.get());
        existingRecurrence.setRecurrenceType(recurrence.getRecurrenceType());
        existingRecurrence.setStartDate(recurrence.getStartDate());
        existingRecurrence.setEndDate(recurrence.getEndDate());
        existingRecurrence.setIntervalAmount(recurrence.getIntervalAmount());

        validateRecurrenceType(existingRecurrence.getRecurrenceType());

        Recurrence updatedRecurrence = recurrenceService.saveRecurrence(existingRecurrence);
        return new ResponseEntity<>(updatedRecurrence, HttpStatus.OK);
    }
    
    //Get all recurrences for a specific user
    @GetMapping("/userReccurence/{userId}")
    public ResponseEntity<List<Recurrence>> getAllRecurrencesByUser(@PathVariable Long userId) {
        List<Recurrence> recurrences = recurrenceService.findRecurrencesByUserId(userId);
        if (recurrences.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(recurrences, HttpStatus.OK);
    }

    // Delete a recurrence
    @DeleteMapping("/{recurrenceId}")
    public ResponseEntity<Void> deleteRecurrence(@PathVariable int recurrenceId) {
        // Check if recurrence exists
        Recurrence existingRecurrence = recurrenceService.findRecurrenceById(recurrenceId);
        if (existingRecurrence == null) {
        	//404
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