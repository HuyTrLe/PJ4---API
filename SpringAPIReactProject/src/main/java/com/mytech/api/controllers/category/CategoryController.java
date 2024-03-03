package com.mytech.api.controllers.category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.models.category.Category;
import com.mytech.api.services.category.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
private final CategoryService categoryService;
	
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Category>> getAllCategoryByUser(@PathVariable Long userId) {
	    List<Category> categories = categoryService.getCategoriesByUserId(userId);
	    if (categories.isEmpty()) {
	        return new ResponseEntity<>(categories, HttpStatus.NOT_FOUND);
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
	@PostMapping("/create")
	public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
		Category category = categoryService.createCategory(categoryRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(category);

	}
}
