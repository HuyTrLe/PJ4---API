package com.mytech.api.controllers.category;

import com.mytech.api.models.category.CateTypeENum;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
	@NotNull(message = "Name cannot be null")
	private String name;
	@NotNull(message = "Type cannot be null")
	private CateTypeENum type;
	@NotNull(message = "Icon cannot be null")
	private Long iconId;
	@NotNull(message = "User cannot be null")
	private Long userId;
}
