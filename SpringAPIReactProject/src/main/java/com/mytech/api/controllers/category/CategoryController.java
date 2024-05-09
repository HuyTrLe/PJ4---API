package com.mytech.api.controllers.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.category.Cat_IconDTO;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.category.CategoryResponse;
import com.mytech.api.models.user.User;
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
	public ResponseEntity<List<CategoryDTO>> getAllCategoryByUser(@PathVariable Long userId) {
		List<CategoryDTO> categories = categoryService.getCategoriesByUserId(userId);
		if (categories.isEmpty()) {
			return new ResponseEntity<>(categories, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(categories, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{categoryId}")
	public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId, Authentication authentication) {
		Category existingCategory = categoryService.getByCateId(categoryId);
		if (existingCategory == null) {
			return ResponseEntity.notFound().build();
		}

		User categoryUser = existingCategory.getUser();
		MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

		if (categoryUser == null || !categoryUser.getId().equals(userDetails.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to delete this category.");
		}

		categoryService.deleteCategoryById(categoryId);
		return ResponseEntity.ok("Category deleted successfully");
	}

	@PostMapping("/create")
	@PreAuthorize("#categoryRequest.userId == authentication.principal.id")
	public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryRequest, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
		}
		CategoryDTO createdCategoryDTO = categoryService.createCategory(categoryRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoryDTO);

	}

	@PutMapping("/update/{categoryId}")
	@PreAuthorize("#updatedCategoryDTO.userId == authentication.principal.id")
	public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,
			@Valid @RequestBody CategoryDTO updatedCategoryDTO, BindingResult result) {
		try {
			if (result.hasErrors()) {
				String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
						.collect(Collectors.joining("\n"));
				System.out.println(errors);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
			}
			CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, updatedCategoryDTO);
			return ResponseEntity.ok(updatedCategory);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	@GetMapping("/icons")
	public ResponseEntity<List<Cat_IconDTO>> getAllIcons() {
		List<Cat_IconDTO> icons = categoryService.getAllIcons();
		return ResponseEntity.ok(icons);
	}

	@GetMapping("/{categoryId}")
	public ResponseEntity<Category> getCategoryById(@PathVariable Long categoryId) {
		Category category = categoryService.getByCateId(categoryId);
		if (category != null) {
			return ResponseEntity.ok(category);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/getCategoryByCategoryId/{categoryId}")
	public ResponseEntity<CategoryResponse> getCategoryByCategoryId(@PathVariable Long categoryId) {
		CategoryResponse category = categoryService.getCategoryByCategoryId(categoryId);
		if (category != null) {
			return ResponseEntity.ok(category);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

}
