package com.mytech.api.services.recurrence;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;

public interface RecurrenceService {

    List<Recurrence> findAllRecurrences();

    List<Recurrence> findRecurrencesByUserId(Long userId);

    Recurrence findRecurrenceById(int recurrenceId);

    ResponseEntity<?> deleteRecurrence(int recurrenceId, Authentication authentication);

    ResponseEntity<?> updateRecurrence(int recurrenceId, RecurrenceDTO recurrenceRequestDTO);

    ResponseEntity<?> createRecurrence(RecurrenceDTO recurrenceRequestDTO);
}