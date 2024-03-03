package com.mytech.api.config;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mytech.api.models.category.Category;
import com.mytech.api.models.category.CategoryDTO;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;

@Configuration
public class ModelMapperConfig {

	@Bean
	ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		modelMapper.createTypeMap(CategoryDTO.class, Category.class)
				.addMappings(mapping -> mapping.map(CategoryDTO::getUserId, Category::setUser))
				.addMappings(mapping -> mapping.map(CategoryDTO::getIconId, Category::setIcon))
				.setPropertyCondition(Conditions.isNotNull());
		modelMapper.createTypeMap(User.class, UserDTO.class)
        .addMapping(User::getId, UserDTO::setId)
        .addMapping(User::getEmail, UserDTO::setEmail)
        .setPropertyCondition(Conditions.isNotNull());

		return modelMapper;
	}
}