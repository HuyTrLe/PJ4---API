package com.mytech.api.services.recurrence;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;

@Service
public class RecurrenceServiceImpl implements RecurrenceService {

	private final RecurrenceRepository recurrenceRepository;

	public RecurrenceServiceImpl(RecurrenceRepository recurrenceRepository) {
		this.recurrenceRepository = recurrenceRepository;
	}

	@Override
	public List<Recurrence> findAllRecurrences() {
		return recurrenceRepository.findAll();
	}

	@Override
	public Recurrence findRecurrenceById(int recurrenceId) {
		return recurrenceRepository.findById(recurrenceId).orElse(null);
	}

	@Override
	public Recurrence saveRecurrence(Recurrence recurrence) {
		return recurrenceRepository.save(recurrence);
	}

	@Override
	public void deleteRecurrenceById(int recurrenceId) {
		recurrenceRepository.deleteById(recurrenceId);
	}

	@Override
	public List<Recurrence> findRecurrencesByUserId(Long userId) {
		return recurrenceRepository.findAllByUserId(userId);
	}

}
