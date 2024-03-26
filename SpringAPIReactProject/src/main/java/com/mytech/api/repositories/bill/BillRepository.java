package com.mytech.api.repositories.bill;

import java.time.LocalDate;

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

	@Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDate < :overdueDueDate")
	Page<Bill> findOverdueBillsByUserId(int userId, LocalDate overdueDueDate, Pageable pageable);

	@Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDate BETWEEN CURRENT_DATE AND :dueDate")
	Page<Bill> findBillsDueIn3DaysByUserId(int userId, LocalDate dueDate, Pageable pageable);

	@Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDate > :futureDueDueDate")
	Page<Bill> findFutureDueBillsByUserId(int userId, LocalDate futureDueDueDate, Pageable pageable);
}