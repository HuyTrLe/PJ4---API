package com.mytech.api.models.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {
	private Long id;
	private String name;
	private CateTypeENum type;
	private Long iconId;
	private Long userId;

	public CategoryDTO(Long id, String name, CateTypeENum type, Long iconId, Long userId) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.iconId = iconId;
		this.userId = userId;
	}
}
