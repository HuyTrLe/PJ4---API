package com.mytech.api.auth.password;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.user.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
        Optional<PasswordResetToken> findByToken(String token);

        Optional<PasswordResetToken> findByUser(User user);

        void deleteByToken(String token);

        @Transactional
        @Modifying
        @Query("UPDATE PasswordResetToken c " +
                        "SET c.confirmedAt = ?2 " +
                        "WHERE c.token = ?1")
        int updateConfirmedAt(String token,
                        LocalDateTime confirmedAt);

}
