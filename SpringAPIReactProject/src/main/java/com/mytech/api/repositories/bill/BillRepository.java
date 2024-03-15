package com.mytech.api.repositories.bill;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.bill.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer>{
	
	@Query("SELECT b FROM Bill b WHERE b.user.id = :userId")
	List<Bill> findAllBillByUserId(long userId);
}
