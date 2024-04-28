package com.mytech.api.services.category;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.category.Cat_Icon;
import com.mytech.api.models.category.Cat_IconDTO;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.user.User;
import com.mytech.api.repositories.categories.CateIconRepository;
import com.mytech.api.repositories.categories.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryServiceImpl implements CategoryService {
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	CateIconRepository catIconRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ModelMapper modelMapper;

	@Transactional
	public void seedCategoriesForNewUsers(User user) {
		if (categoryRepository.countByUser(user) == 0) {
			seedCatIcons();
			List<Cat_Icon> icons = catIconRepository.findAll();
			List<Category> defaultCategories = createDefaultCategories(icons, user);
			if (categoryRepository.existsByUserAndIconIn(user, icons)) {
				return;
			}

			categoryRepository.saveAll(defaultCategories);
		}

	}

	private void seedCatIcons() {
		if (catIconRepository.count() == 0) {
			catIconRepository.save(new Cat_Icon("anotherbill.png"));
			catIconRepository.save(new Cat_Icon("beauty.png"));
			catIconRepository.save(new Cat_Icon("bill&fees.png"));
			catIconRepository.save(new Cat_Icon("business.png"));
			catIconRepository.save(new Cat_Icon("drink.png"));
			catIconRepository.save(new Cat_Icon("food.png"));
			catIconRepository.save(new Cat_Icon("education.png"));
			catIconRepository.save(new Cat_Icon("entertainment.png"));
			catIconRepository.save(new Cat_Icon("extraincome.png"));
			catIconRepository.save(new Cat_Icon("gift.png"));
			catIconRepository.save(new Cat_Icon("grocery.png"));
			catIconRepository.save(new Cat_Icon("home.png"));
			catIconRepository.save(new Cat_Icon("homebill.png"));
			catIconRepository.save(new Cat_Icon("loan.png"));
			catIconRepository.save(new Cat_Icon("other.png"));
			catIconRepository.save(new Cat_Icon("phonebill.png"));
			catIconRepository.save(new Cat_Icon("salary.png"));
			catIconRepository.save(new Cat_Icon("shopping.png"));
			catIconRepository.save(new Cat_Icon("transport.png"));
			catIconRepository.save(new Cat_Icon("travel.png"));
			catIconRepository.save(new Cat_Icon("waterbill.png"));
		}
	}

	private List<Category> createDefaultCategories(List<Cat_Icon> icons, User user) {
		return Arrays.asList(new Category("Another Bill", CateTypeENum.EXPENSE, icons.get(0), user),
				new Category("Beauty", CateTypeENum.EXPENSE, icons.get(1), user),
				new Category("Bills & Fees", CateTypeENum.EXPENSE, icons.get(2), user),
				new Category("Business", CateTypeENum.INCOME, icons.get(3), user),
				new Category("Drink", CateTypeENum.EXPENSE, icons.get(4), user),
				new Category("Food", CateTypeENum.EXPENSE, icons.get(5), user),
				new Category("Education", CateTypeENum.EXPENSE, icons.get(6), user),
				new Category("Entertainment", CateTypeENum.EXPENSE, icons.get(7), user),
				new Category("Extra Income", CateTypeENum.INCOME, icons.get(8), user),
				new Category("Gift", CateTypeENum.INCOME, icons.get(9), user),
				new Category("Grocery", CateTypeENum.EXPENSE, icons.get(10), user),
				new Category("Home", CateTypeENum.EXPENSE, icons.get(11), user),
				new Category("Home Bill", CateTypeENum.EXPENSE, icons.get(12), user),
				new Category("Loan", CateTypeENum.DEBT, icons.get(13), user),
				new Category("Other Income", CateTypeENum.INCOME, icons.get(14), user),
				new Category("Phone Bill", CateTypeENum.EXPENSE, icons.get(15), user),
				new Category("Salary", CateTypeENum.INCOME, icons.get(16), user),
				new Category("Shopping", CateTypeENum.EXPENSE, icons.get(17), user),
				new Category("Transport", CateTypeENum.EXPENSE, icons.get(18), user),
				new Category("Travel", CateTypeENum.EXPENSE, icons.get(19), user),
				new Category("Water Bill", CateTypeENum.EXPENSE, icons.get(20), user));

	}

	@Override
	public List<CategoryDTO> getCategoriesByUserId(Long userId) {
		List<Category> categories = categoryRepository.findByUserId(userId);
		System.out.println("Retrieved categories: " + categories);
		List<CategoryDTO> categoryDTOs = categories.stream()
				.map(category -> modelMapper.map(category, CategoryDTO.class)).collect(Collectors.toList());

		return categoryDTOs;
	}

	@Override
	@Transactional
	public void deleteCategoryById(Long categoryId) {
		if (existsCategoryById(categoryId)) {
			categoryRepository.deleteById(categoryId);
		} else {
			throw new EntityNotFoundException("Category not found");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsCategoryById(Long categoryId) {
		return categoryRepository.existsById(categoryId);
	}

	@Override
	public CategoryDTO createCategory(CategoryDTO categoryDTO) {
		User user = userRepository.findById(categoryDTO.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		Cat_Icon catIcon = catIconRepository.findById(categoryDTO.getIcon().getId())
				.orElseThrow(() -> new IllegalArgumentException("Icon not found"));
		Category category = modelMapper.map(categoryDTO, Category.class);
		category.setUser(user);
		category.setIcon(catIcon);

		Category createdCategory = categoryRepository.save(category);

		return modelMapper.map(createdCategory, CategoryDTO.class);
	}

	@Override
	@Transactional
	public CategoryDTO updateCategory(Long categoryId, CategoryDTO updateCategoryDTO) {
		try {
			Category existingCategory = categoryRepository.findById(categoryId)
					.orElseThrow(() -> new IllegalArgumentException("Category not found"));

			// Check if the categoryType is being updated
			if (!existingCategory.getType().equals(updateCategoryDTO.getType())) {
				throw new IllegalArgumentException("Cannot update category type");
			}

			// Copy the fields that can be updated
			existingCategory.setName(updateCategoryDTO.getName());

			User user = userRepository.findById(updateCategoryDTO.getUserId())
					.orElseThrow(() -> new IllegalArgumentException("User not found"));
			Cat_Icon catIcon = catIconRepository.findById(updateCategoryDTO.getIcon().getId())
					.orElseThrow(() -> new IllegalArgumentException("Icon not found"));

			existingCategory.setUser(user);
			existingCategory.setIcon(catIcon);

			Category updatedCategory = categoryRepository.save(existingCategory);

			return modelMapper.map(updatedCategory, CategoryDTO.class);
		} catch (Exception e) {
			throw new RuntimeException("Error updating category", e);
		}
	}

	@Override
	@Transactional
	public List<Cat_IconDTO> getAllIcons() {
		List<Cat_Icon> icons = catIconRepository.findAll();
		List<Cat_IconDTO> iconsDTO = icons.stream().map(category -> modelMapper.map(category, Cat_IconDTO.class))
				.collect(Collectors.toList());

		return iconsDTO;
	}

	@Override
	public Category getByCateId(Long categoryId) {
		Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
		if (!categoryOptional.isPresent()) {
			throw new IllegalArgumentException("Category not found with ID: " + categoryId);
		}

		Category category = categoryOptional.get();
		// Assume we handle the category even if it does not have an associated user
		Category resultCategory = new Category();
		resultCategory.setId(category.getId());
		resultCategory.setName(category.getName());
		resultCategory.setUser(category.getUser()); // Copy the user as is, could be null

		return resultCategory;
	}

}
