package com.mytech.api.services.category;

import java.util.List;

import com.mytech.api.controllers.category.CategoryRequest;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.user.User;


public interface CategoryService {

	List<Category> getCategoriesByUserId(Long userId);
	
	void seedCategoriesForNewUsers(User user);
	
	void deleteCategoryById(Long categoryId);
	
	boolean existsCategoryById(Long categoryId);
	
	Category createCategory(CategoryRequest categoryRequest);
	
	Category updateCategory(Long categoryId, CategoryRequest updateCategoryRequest);
}
