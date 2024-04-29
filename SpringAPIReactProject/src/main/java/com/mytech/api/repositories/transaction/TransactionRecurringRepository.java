package com.mytech.api.repositories.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.transaction.TransactionRecurring;

@Repository
public interface TransactionRecurringRepository extends JpaRepository<TransactionRecurring, Integer> {

    @Query("SELECT t FROM TransactionRecurring t WHERE t.user.id = :userId")
    Page<TransactionRecurring> findByUserId(Integer userId, Pageable pageable);

    List<TransactionRecurring> findByRecurrence_DueDate(LocalDate dueDate);
}
