package com.mytech.api.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.category.Cat_Icon;

@Repository
public interface CategoryIconRepository extends JpaRepository<Cat_Icon, Long>{

}
