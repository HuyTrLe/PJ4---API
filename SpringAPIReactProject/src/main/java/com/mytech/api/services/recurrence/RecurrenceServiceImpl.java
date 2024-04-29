package com.mytech.api.services.recurrence;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceConverter;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.repositories.recurrence.RecurrenceRepository;

@Service
public class RecurrenceServiceImpl implements RecurrenceService {

	@Autowired
	RecurrenceRepository recurrenceRepository;
	@Autowired
	RecurrenceConverter recurrenceConverter;
	@Autowired
	ModelMapper modelMapper;

	@Override
	public List<Recurrence> findAllRecurrences() {
		return recurrenceRepository.findAll();
	}

	@Override
	public Recurrence findRecurrenceById(int recurrenceId) {
		return recurrenceRepository.findById(recurrenceId).orElse(null);
	}

	@Override
	public List<Recurrence> findRecurrencesByUserId(Long userId) {
		return recurrenceRepository.findAllByUserId(userId);
	}

	@Override
	public ResponseEntity<?> createRecurrence(RecurrenceDTO recurrenceRequestDTO) {

		try {
			Recurrence recurrence = recurrenceConverter.convertToEntity(recurrenceRequestDTO);
			recurrence.setStartDate(recurrenceRequestDTO.getStartDate());

			Recurrence createdRecurrence = recurrenceRepository.save(recurrence);
			RecurrenceDTO newRecurrenceDTO = convertToDTO(createdRecurrence);
			return new ResponseEntity<>(newRecurrenceDTO, HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> updateRecurrence(int recurrenceId, RecurrenceDTO recurrenceRequestDTO) {
		try {
			Recurrence existingRecurrence = recurrenceRepository.findById(recurrenceId).orElse(null);
			if (existingRecurrence == null) {
				return new ResponseEntity<>("Recurrence not found", HttpStatus.NOT_FOUND);
			}

			Recurrence updatedRecurrence = recurrenceConverter.convertToEntity(recurrenceRequestDTO);
			updatedRecurrence.setStartDate(recurrenceRequestDTO.getStartDate());
			Recurrence savedRecurrence = recurrenceRepository.save(updatedRecurrence);
			return ResponseEntity.ok(savedRecurrence);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> deleteRecurrence(int recurrenceId, Authentication authentication) {
		Recurrence existingRecurrence = recurrenceRepository.findById(recurrenceId).orElse(null);
		MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
		if (!existingRecurrence.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to delete this transaction.");
		}
		if (existingRecurrence == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			recurrenceRepository.deleteById(recurrenceId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	private RecurrenceDTO convertToDTO(Recurrence recurrence) {
		return modelMapper.map(recurrence, RecurrenceDTO.class);
	}

}
