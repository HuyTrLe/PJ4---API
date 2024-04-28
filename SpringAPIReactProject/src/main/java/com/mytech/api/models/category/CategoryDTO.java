package com.mytech.api.models.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {
	private Long id;

	@NotBlank(message = "Category name cannot be blank")
	private String name;
	private CateTypeENum type;
	private Cat_IconDTO icon;
	private Long userId;

	public CategoryDTO(Long id, String name, CateTypeENum type, Cat_IconDTO icon, Long userId) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.icon = icon;
		this.userId = userId;
	}
}
