package com.mytech.api.repositories.recurrence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.recurrence.Recurrence;

@Repository
public interface RecurrenceRepository extends JpaRepository<Recurrence, Integer> {

	@Query("SELECT r FROM Recurrence r WHERE r.user.id = :userId")
	List<Recurrence> findAllByUserId(Long userId);

}
