package com.mytech.api.services.category;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.User;
import com.mytech.api.models.category.Cat_Icon;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.repositories.categories.CateIconRepository;
import com.mytech.api.repositories.categories.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryServiceImpl implements CategoryService {
	private final CategoryRepository categoryRepository;
	private final CateIconRepository catIconRepository;
	private final UserRepository userRepository;

	public CategoryServiceImpl(CategoryRepository categoryRepository, CateIconRepository catIconRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.catIconRepository = catIconRepository;
		this.userRepository = userRepository;
	}

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
			catIconRepository.save(new Cat_Icon("./assets/img/icons/anotherbill.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/beauty.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/bill&fees.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/business.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/drink.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/food.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/education.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/entertainment.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/extraincome.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/gift.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/grocery.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/home.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/homebill.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/loan.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/other.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/phonebill.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/salary.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/shopping.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/transport.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/travel.png"));
			catIconRepository.save(new Cat_Icon("./assets/img/icons/waterbill.png"));
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
				new Category("Gift", CateTypeENum.EXPENSE, icons.get(9), user));
	}

	@Override
	public List<Category> getCategoriesByUserId(Long userId) {
	    System.out.println("Fetching categories for user ID: " + userId);
	    User user = userRepository.findById(userId).orElse(null);
	    System.out.println("User details: " + user);
	    List<Category> categories = categoryRepository.findByUserId(userId);
	    System.out.println("Retrieved categories: " + categories);
	    return categories;
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
}
