package com.mytech.api.services.category;

import java.util.List;

import com.mytech.api.models.User;
import com.mytech.api.models.category.Category;


public interface CategoryService {

	List<Category> getCategoriesByUserId(Long userId);
	
	void seedCategoriesForNewUsers(User user);
	
	void deleteCategoryById(Long categoryId);
	
	boolean existsCategoryById(Long categoryId);
}
