package com.mytech.api.repositories.bill;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.bill.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {

    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId")
    Page<Bill> findAllBillByUserId(int userId, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDate < :currentDate")
    Page<Bill> findOverdueBillsByUserId(@Param("userId") int userId, @Param("currentDate") LocalDate currentDate, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDate >= :currentDate AND b.dueDate <= :dueDate")
    Page<Bill> findBillsDueIn3DaysByUserId(@Param("userId") int userId, @Param("currentDate") LocalDate currentDate, @Param("dueDate") LocalDate dueDate, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDate > :futureDueDate")
    Page<Bill> findFutureDueBillsByUserId(@Param("userId") int userId, @Param("futureDueDate") LocalDate futureDueDate, Pageable pageable);
}
