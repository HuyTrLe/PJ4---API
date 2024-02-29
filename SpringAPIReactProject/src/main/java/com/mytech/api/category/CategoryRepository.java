package com.mytech.api.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

}
