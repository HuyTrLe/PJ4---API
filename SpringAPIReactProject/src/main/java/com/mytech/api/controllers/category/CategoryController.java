package com.mytech.api.controllers.category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.models.category.Category;
import com.mytech.api.services.category.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
private final CategoryService categoryService;
	
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Category>> getAllCategoryByUser(@PathVariable Long userId) {
	    System.out.println("Controller - User ID: " + userId);
	    List<Category> categories = categoryService.getCategoriesByUserId(userId);
	    System.out.println("Controller - Categories size: " + categories.size());
	    if (categories.isEmpty()) {
	        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	    }
	    return new ResponseEntity<>(categories, HttpStatus.OK);
	}
	
	@DeleteMapping("/{categoryId}")
	public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
	    if (categoryService.existsCategoryById(categoryId)) {
	        categoryService.deleteCategoryById(categoryId);
	        return ResponseEntity.ok("Category deleted successfully");
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
	    }
	}
}
