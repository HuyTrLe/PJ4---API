package com.mytech.api.repositories.categories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.category.Cat_Icon;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.category.CategoryResponse;
import com.mytech.api.models.user.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT r FROM Category r WHERE r.user.id = :userId")
	List<Category> findByUserId(Long userId);

	@Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.id = :categoryId")
	boolean existsById(@Param("categoryId") Long categoryId);

	long countByUser(User user);

	boolean existsByUserAndIconIn(User user, List<Cat_Icon> icons);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	void deleteCategoryById(Long categoryId);
		
	@Query("SELECT new com.mytech.api.models.category.CategoryResponse(c.id, c.name, c.type, ci.path, us.id) FROM Category c "
			+ "JOIN c.icon ci "
			+ "JOIN c.user us "
			+ "WHERE c.id = :id")
	CategoryResponse getCategoryByCategoryId(Long id);

	Category findFirstByName(String categoryName);
	
	List<Category> findByName(String categoryName);
	
	List<Category> findByNameAndUserId(String name, Long userId);

}