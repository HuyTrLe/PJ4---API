package com.mytech.api.repositories.bill;

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
}
