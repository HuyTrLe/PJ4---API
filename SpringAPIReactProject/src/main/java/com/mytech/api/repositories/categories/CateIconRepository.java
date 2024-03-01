package com.mytech.api.repositories.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.category.Cat_Icon;

@Repository
public interface CateIconRepository extends JpaRepository<Cat_Icon, Long>{

}
