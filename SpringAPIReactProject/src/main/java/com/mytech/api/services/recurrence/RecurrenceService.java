package com.mytech.api.services.recurrence;

import com.mytech.api.models.recurrence.Recurrence;
import java.util.List;

public interface RecurrenceService {

    List<Recurrence> findAllRecurrences();
    
    List<Recurrence> findRecurrencesByUserId(Long userId);

    Recurrence findRecurrenceById(int recurrenceId);

    Recurrence saveRecurrence(Recurrence recurrence);

    void deleteRecurrenceById(int recurrenceId);
}