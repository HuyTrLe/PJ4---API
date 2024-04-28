package com.mytech.api.auth.payload.request.token.emailupdate;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EmailUpdateConfirmationTokenRepository extends JpaRepository<EmailUpdateConfirmationToken, Long> {
	Optional<EmailUpdateConfirmationToken> findByToken(String token);

	@Transactional
	@Modifying
	@Query("UPDATE ConfirmationToken c " +
			"SET c.confirmedAt = ?2 " +
			"WHERE c.token = ?1")
	int updateConfirmedAt(String token,
			LocalDateTime confirmedAt);
}
