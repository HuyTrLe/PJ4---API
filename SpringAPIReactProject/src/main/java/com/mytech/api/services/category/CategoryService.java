package com.mytech.api.services.category;

import java.util.List;

import com.mytech.api.models.category.Cat_IconDTO;
import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.user.User;


public interface CategoryService {

	List<CategoryDTO> getCategoriesByUserId(Long userId);
	
	void seedCategoriesForNewUsers(User user);
	
	void deleteCategoryById(Long categoryId);
	
	boolean existsCategoryById(Long categoryId);
	
	CategoryDTO createCategory(CategoryDTO categoryRequest);
	
	CategoryDTO updateCategory(Long categoryId, CategoryDTO updateCategoryDTO);
	
	List<Cat_IconDTO> getAllIcons();
}
