package com.mytech.api.repositories.categories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.User;
import com.mytech.api.models.category.Cat_Icon;
import com.mytech.api.models.category.Category;

import jakarta.transaction.Transactional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Transactional
    @Modifying
	@Query("SELECT r FROM Category r WHERE r.user.id = :userId")
	List<Category> findByUserId(Long userId);
	
	
	long countByUser(User user);
	boolean existsByUserAndIconIn(User user, List<Cat_Icon> icons);
}