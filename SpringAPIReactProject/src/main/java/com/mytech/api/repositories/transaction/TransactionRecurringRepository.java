package com.mytech.api.repositories.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.transaction.TransactionRecurring;

@Repository
public interface TransactionRecurringRepository extends JpaRepository<TransactionRecurring, Integer> {

    @Query("SELECT t FROM TransactionRecurring t WHERE t.user.id = :userId")
    Page<TransactionRecurring> findByUserId(Integer userId, Pageable pageable);

    List<TransactionRecurring> findByRecurrence_DueDate(LocalDate dueDate);
    
    @Query("SELECT b FROM TransactionRecurring b " +
            "WHERE (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'DAILY' AND :currentDate <= b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'WEEKLY' AND :currentDate <= b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'MONTHLY' AND :currentDate <= b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'YEARLY' AND :currentDate <= b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'TIMES' AND :currentDate <= b.recurrence.dueDate) OR (b.recurrence.endType = 'FOREVER') AND b.user.id = :userId ORDER BY b.id desc")
    List<TransactionRecurring> findRecuActive(int userId,LocalDate currentDate);
    @Query("SELECT b FROM TransactionRecurring b " +
            "WHERE (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'DAILY' AND :currentDate > b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'WEEKLY' AND :currentDate > b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'MONTHLY' AND :currentDate > b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'YEARLY' AND :currentDate > b.recurrence.endDate) " +
            "OR (b.recurrence.endType = 'TIMES' AND :currentDate > b.recurrence.dueDate) AND b.user.id = :userId ORDER BY b.id desc")
    List<TransactionRecurring> findRecuExpired (int userId,LocalDate currentDate);
}
