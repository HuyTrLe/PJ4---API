package com.mytech.api.repositories.bill;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.bill.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {

        @Query("SELECT b FROM Bill b WHERE b.user.id = :userId")
        Page<Bill> findAllBillByUserId(int userId, Pageable pageable);

        List<Bill> findByRecurrence_RecurrenceId(int recurrenceId);

        List<Bill> findByRecurrence_DueDate(LocalDate dueDate);
        
        @Query("SELECT b FROM Bill b " +
                "WHERE (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'DAILY' AND :currentDate < b.recurrence.endDate) " +
                "OR (b.recurrence.endType = 'TIMES' AND :currentDate < b.recurrence.dueDate) OR (b.recurrence.endType = 'FOREVER') AND b.user.id = :userId")
        List<Bill> findBillActive(int userId,LocalDate currentDate);
        @Query("SELECT b FROM Bill b " +
                "WHERE (b.recurrence.endType = 'UNTIL' AND b.recurrence.frequencyType = 'DAILY' AND :currentDate > b.recurrence.endDate) " +
                "OR (b.recurrence.endType = 'TIMES' AND :currentDate > b.recurrence.dueDate) AND b.user.id = :userId")
        List<Bill> findBillExpired (int userId,LocalDate currentDate);
}
