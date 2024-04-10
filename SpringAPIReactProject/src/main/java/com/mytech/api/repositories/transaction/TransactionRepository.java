package com.mytech.api.repositories.transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.transaction.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer>{
	
	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
	List<Transaction> findByUserId(int userId);
}
