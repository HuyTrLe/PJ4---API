package com.mytech.api.controllers.recurrence;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.services.recurrence.RecurrenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recurrences")
public class RecurrenceController {

    @Autowired
    RecurrenceService recurrenceService;
    @Autowired
    ModelMapper modelMapper;

    // Schedule a new recurring transaction/bill
    @PostMapping("/create")
    @PreAuthorize("#recurrenceRequestDTO.userId == authentication.principal.id")
    public ResponseEntity<?> createRecurrence(@RequestBody @Valid RecurrenceDTO recurrenceRequestDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        return recurrenceService.createRecurrence(recurrenceRequestDTO);
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
    @PreAuthorize("#recurrenceRequestDTO.userId == authentication.principal.id")
    public ResponseEntity<?> updateRecurrence(@PathVariable int recurrenceId,
            @RequestBody @Valid RecurrenceDTO recurrenceRequestDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        return recurrenceService.updateRecurrence(recurrenceId, recurrenceRequestDTO);
    }

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
    public ResponseEntity<?> deleteRecurrence(@PathVariable int recurrenceId, Authentication authentication) {
        return deleteRecurrence(recurrenceId, authentication);
    }

}